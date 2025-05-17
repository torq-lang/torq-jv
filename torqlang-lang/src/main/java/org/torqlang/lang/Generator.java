/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

/*
 * Generator transforms statements and expression into kernel instructions using the visitor pattern.
 *
 * Node Processing
 * ===============
 *
 * BASIC PROCESSING:
 *
 * -- A node generates its kernel instructions into the given target scope.
 * -- If a node traverses other nodes, it creates and offers a child scope when it traverses them.
 *
 * EXPRESSION NODES:
 *
 * -- A child node always returns a value or identifier so that the caller can use the value or identifier as an
 *    argument.
 * -- If a child node is a sub-expression, the parent node may offer an identifier. If offered an identifier, the child
 *    node binds its result to the offered identifier and clears the offering by calling
 *    `LocalTarget.clearOfferedIdent()`. If not offered an identifier, the child node creates an identifier and adds it
 *    to the target scope. Finally, the child node returns either the offered identifier or created identifier.
 * -- If a child node is an identifier or value and is offered an identifier, it binds itself to the offered
 *    identifier and returns either the offered identifier or itself.
 *
 * STATEMENT NODES:
 * -- Statement nodes are not offered identifiers because they are not "values".
 * -- Body sequences are visited such that only the last entry in the sequence can be offered an identifier. If the
 *    body is an expression, it is offered an identifier. Otherwise, no identifier is offered.
 *
 * Ident and Value Expression Processing
 * =====================================
 *
 * Node processing for values and idents follow the same processing pattern. If no ident is offered, simply return the
 * contained value or ident to be used as an argument by the caller. Otherwise, bind the value or ident to the offered
 * ident and return the offered ident to be used as an argument by the caller.
 *
 * -- visitBoolAsExpr
 * -- visitCharAsExpr
 * -- visitDec128AsExpr
 * -- visitEofAsExpr
 * -- visitFltAsExpr
 * -- visitIdentAsExpr
 * -- visitIntAsExpr
 * -- visitNullAsExpr
 * -- visitStrAsExpr
 */
public final class Generator implements LangVisitor<LocalTarget, CompleteOrIdent> {

    public static final int BREAK_ID = 1;
    public static final int CONTINUE_ID = 2;
    public static final int RETURN_ID = 3;

    public static final Str NEW = Str.of("new");
    public static final Str ERROR = Str.of("error");
    public static final Str NAME = Str.of("name");
    public static final Str MESSAGE = Str.of("message");
    public static final Str DETAILS = Str.of("details");
    public static final Str REQUEST = Str.of("request");
    public static final Str NOTIFY = Str.of("notify");
    public static final Str HANDLERS = Str.of("handlers");

    public static final String ASK_NOT_HANDLED_ERROR_NAME = "org.torqlang.lang.AskNotHandledError";
    public static final String ASK_NOT_HANDLED_ERROR_MESSAGE = """
        Actor could not match request message with an 'ask' handler.""";
    public static final String TELL_NOT_HANDLED_ERROR_NAME = "org.torqlang.lang.TellNotHandledError";
    public static final String TELL_NOT_HANDLED_ERROR_MESSAGE = """
        Actor could not match notify message with a 'tell' handler.""";

    private int nextSystemAnonymousSuffix = 0;
    private int nextSystemVarSuffix = 0;

    private static IdentAsPat assertIdentAsPatNotEscaped(Pat pat) {
        if (pat instanceof IdentAsPat identAsPat) {
            if (identAsPat.escaped) {
                throw new InvalidEscapeError(identAsPat);
            }
        } else {
            throw new NotIdentError(pat);
        }
        return identAsPat;
    }

    private static void compileParamsToIdents(List<Pat> params, List<Ident> idents) {
        for (Pat param : params) {
            if (param instanceof IdentAsPat identAsPat) {
                if (identAsPat.escaped) {
                    throw new InvalidEscapeError(param);
                }
                idents.add(identAsPat.ident);
            } else {
                throw new NotIdentError(param);
            }
        }
    }

    private static SeqLang createElseUnhandledSeq(ActorLang lang, Ident errorIdent, String errorName,
                                                  String errorMessage, RecExpr errorDetails, SourceSpan endOfActorSpan)
    {
        VarStmt errorVar = new VarStmt(
            List.of(
                new IdentVarDecl(new IdentAsPat(errorIdent, false, endOfActorSpan), endOfActorSpan)
            ),
            endOfActorSpan
        );
        RecExpr errorExpr = new RecExpr(
            new StrAsExpr(ERROR, endOfActorSpan),
            List.of(
                new FieldExpr(
                    new StrAsExpr(NAME, endOfActorSpan),
                    new StrAsExpr(Str.of(errorName), endOfActorSpan),
                    endOfActorSpan
                ),
                new FieldExpr(
                    new StrAsExpr(MESSAGE, endOfActorSpan),
                    new StrAsExpr(Str.of(errorMessage), endOfActorSpan),
                    endOfActorSpan
                ),
                new FieldExpr(
                    new StrAsExpr(DETAILS, endOfActorSpan),
                    errorDetails,
                    endOfActorSpan
                )
            ),
            endOfActorSpan
        );
        UnifyStmt errorBind = new UnifyStmt(new IdentAsExpr(errorIdent, endOfActorSpan), errorExpr, endOfActorSpan);
        ThrowLang errorThrow = new ThrowLang(new IdentAsExpr(errorIdent, endOfActorSpan), endOfActorSpan);
        return new SeqLang(List.of(errorVar, errorBind, errorThrow), lang);
    }

    public final Instr acceptExpr(StmtOrExpr stmtOrExpr, Ident exprIdent) throws Exception {
        LocalTarget target = LocalTarget.createExprTargetForRoot(exprIdent);
        stmtOrExpr.accept(this, target);
        return target.build();
    }

    private Ident acceptOfferedIdentOrNextSystemVarIdent(LocalTarget target) {
        Ident varIdent;
        if (target.offeredIdent() != null) {
            varIdent = target.offeredIdent();
            target.acceptOfferedIdent();
        } else {
            varIdent = allocateNextSystemVarIdent();
            target.addIdentDef(new IdentDef(varIdent));
        }
        return varIdent;
    }

    private Ident acceptOfferedIdentOrNull(LocalTarget target) {
        Ident varIdent = target.offeredIdent();
        if (varIdent == null) {
            return null;
        }
        target.acceptOfferedIdent();
        return varIdent;
    }

    public final Instr acceptStmt(StmtOrExpr stmtOrExpr) throws Exception {
        LocalTarget target = LocalTarget.createStmtTargetForRoot();
        stmtOrExpr.accept(this, target);
        return target.build();
    }

    final Ident allocateNextSystemAnonymousIdent() {
        int next = nextSystemAnonymousSuffix;
        nextSystemAnonymousSuffix++;
        return Ident.createSystemAnonymousIdent(next);
    }

    final Ident allocateNextSystemVarIdent() {
        int next = nextSystemVarSuffix;
        nextSystemVarSuffix++;
        return Ident.createSystemVarIdent(next);
    }

    private void buildActorInstrs(Ident exprIdent, ActorLang lang, LocalTarget target) throws Exception {

        SourceSpan endOfActorSpan = lang.toSourceEnd();

        target.addIdentDef(new IdentDef(Ident.$ACTOR_CFGTR));
        LocalTarget childTarget = target.asStmtTargetWithNewScope();

        // --- Build the configurator
        List<Pat> params = lang.params;
        List<Ident> xs = new ArrayList<>(params.size() + 1);
        compileParamsToIdents(params, xs);
        xs.add(Ident.$R);
        // Initializer
        LocalTarget actorBodyTarget = childTarget.asStmtTargetWithNewScope();
        for (StmtOrExpr next : lang.initializer()) {
            next.accept(this, actorBodyTarget);
        }
        // Ask handlers
        Ident askProcIdent = allocateNextSystemVarIdent();
        actorBodyTarget.addIdentDef(new IdentDef(askProcIdent));
        RecExpr askErrorDetails = new RecExpr(
            List.of(
                new FieldExpr(
                    new StrAsExpr(REQUEST, endOfActorSpan),
                    new IdentAsExpr(Ident.$M, endOfActorSpan),
                    endOfActorSpan
                )
            ),
            endOfActorSpan
        );
        buildHandlersProc(lang, askProcIdent, lang.askHandlers(), ASK_NOT_HANDLED_ERROR_NAME, ASK_NOT_HANDLED_ERROR_MESSAGE, askErrorDetails, actorBodyTarget);
        // Tell handlers
        Ident tellProcIdent = allocateNextSystemVarIdent();
        actorBodyTarget.addIdentDef(new IdentDef(tellProcIdent));
        RecExpr tellErrorDetails = new RecExpr(
            List.of(
                new FieldExpr(
                    new StrAsExpr(NOTIFY, endOfActorSpan),
                    new IdentAsExpr(Ident.$M, endOfActorSpan),
                    endOfActorSpan
                )
            ),
            endOfActorSpan
        );
        buildHandlersProc(lang, tellProcIdent, lang.tellHandlers(), TELL_NOT_HANDLED_ERROR_NAME, TELL_NOT_HANDLED_ERROR_MESSAGE, tellErrorDetails, actorBodyTarget);
        // Create and bind handlers tuple to result
        List<ValueDef> handlers = List.of(new ValueDef(askProcIdent, endOfActorSpan),
            new ValueDef(tellProcIdent, endOfActorSpan));
        TupleDef handlersDef = new TupleDef(HANDLERS, handlers, endOfActorSpan);
        actorBodyTarget.addInstr(new CreateTupleInstr(Ident.$R, handlersDef, endOfActorSpan));
        // --- Build body containing initializer, ask handlers, and tell handlers
        Instr bodyInstr = actorBodyTarget.build();
        ProcDef actorCfgtrDef = new ProcDef(xs, bodyInstr, lang);
        childTarget.addInstr(new CreateActorCfgtrInstr(Ident.$ACTOR_CFGTR, actorCfgtrDef, lang));

        // Build the actor record
        FieldDef configDef = new FieldDef(NEW, Ident.$ACTOR_CFGTR, endOfActorSpan);
        RecDef actorRecDef = new RecDef(Str.of(exprIdent.name), List.of(configDef), endOfActorSpan);
        childTarget.addInstr(new CreateRecInstr(exprIdent, actorRecDef, endOfActorSpan));

        target.addInstr(childTarget.build());
    }

    private CompleteOrIdent buildBodyInstrs(List<StmtOrExpr> bodyList, LocalTarget target) throws Exception {
        int sizeMinusOne = bodyList.size() - 1;
        // Do not offer intermediate nodes a target identifier
        LocalTarget stmtTarget = target.asStmtTargetWithSameScope();
        for (int i = 0; i < sizeMinusOne; i++) {
            StmtOrExpr next = bodyList.get(i);
            next.accept(this, stmtTarget);
        }
        StmtOrExpr last = bodyList.get(sizeMinusOne);
        // Only offer the last node the target identifier (if one exists)
        return last.accept(this, target);
    }

    private Instr buildCaseInstrs(CompleteOrIdent arg, ValueOrPtn valueOrPtn, List<CompiledPat.ChildPtn> childPtns,
                                  int childPtnNext, MatchClause matchClause, boolean elseNeeded, SourceSpan elseSpan,
                                  Ident exprIdent, LocalTarget target) throws Exception
    {
        // BUILD CASE BODY

        LocalTarget caseBodyTarget;
        if (exprIdent != null) {
            caseBodyTarget = target.asExprTargetWithNewScope(exprIdent);
        } else {
            caseBodyTarget = target.asStmtTargetWithNewScope();
        }
        Instr caseBodyInstr;
        if (childPtnNext < childPtns.size()) {
            CompiledPat.ChildPtn nextChild = childPtns.get(childPtnNext);
            caseBodyInstr = buildCaseInstrs(nextChild.arg, nextChild.recPtn, childPtns,
                childPtnNext + 1, matchClause, elseNeeded, elseSpan, exprIdent, caseBodyTarget);
        } else {
            if (matchClause.guard != null) {
                buildMatchClauseWithGuard(matchClause, elseNeeded, elseSpan, exprIdent, caseBodyTarget);
            } else {
                matchClause.accept(this, caseBodyTarget);
            }
            caseBodyInstr = caseBodyTarget.build();
        }

        // CREATE AND RETURN CASE INSTR

        Instr caseInstr;
        if (elseNeeded) {
            Instr applyElseInstr;
            if (exprIdent != null) {
                applyElseInstr = new ApplyInstr(Ident.$ELSE, List.of(exprIdent), elseSpan);
            } else {
                applyElseInstr = new ApplyInstr(Ident.$ELSE, List.of(), elseSpan);
            }
            caseInstr = new CaseElseInstr(arg, valueOrPtn, caseBodyInstr, applyElseInstr, matchClause.body);
        } else {
            caseInstr = new CaseInstr(arg, valueOrPtn, caseBodyInstr, matchClause.body);
        }
        return caseInstr;
    }

    @SuppressWarnings("unchecked")
    private void buildHandlersProc(ActorLang lang, Ident targetIdent, List<? extends MatchClause> handlers,
                                   String notHandledErrorName, String notHandledErrorMessage,
                                   RecExpr notHandledErrorDetails, LocalTarget target)
        throws Exception
    {
        SourceSpan endOfActorSpan = lang.toSourceEnd();
        LocalTarget handlerBodyTarget = target.asStmtTargetWithNewScope();
        // If there are no handlers, generate kernel instructions to throw an error
        if (handlers.isEmpty()) {
            Ident errorIdent = allocateNextSystemVarIdent();
            SeqLang unhandledSeq = createElseUnhandledSeq(lang, errorIdent, notHandledErrorName,
                notHandledErrorMessage, notHandledErrorDetails, endOfActorSpan);
            unhandledSeq.accept(this, handlerBodyTarget);
            Instr handlerThrowInstr = handlerBodyTarget.build();
            target.addInstr(new CreateProcInstr(targetIdent, new ProcDef(List.of(Ident.$M),
                handlerThrowInstr, lang), lang));
        } else {
            // Synthesize an "else" to throw an error if `$m` is not matched
            Ident errorIdent = allocateNextSystemVarIdent();
            SeqLang elseUnhandledSeq = createElseUnhandledSeq(lang, errorIdent, notHandledErrorName,
                notHandledErrorMessage, notHandledErrorDetails, endOfActorSpan);
            // Generate match logic using case instructions
            visitMatchClauses(Ident.$M, handlers.get(0), (List<MatchClause>) handlers, 1,
                elseUnhandledSeq, null, handlerBodyTarget);
            // Add a jump-catch if `return` was used during an `ask`
            if (handlerBodyTarget.isReturnUsed()) {
                handlerBodyTarget.addInstr(new JumpCatchInstr(RETURN_ID, endOfActorSpan));
            }
            Instr handlerCaseInstr = handlerBodyTarget.build();
            target.addInstr(new CreateProcInstr(targetIdent, new ProcDef(List.of(Ident.$M),
                handlerCaseInstr, lang), lang));
        }
    }

    private void buildIfInstrsRecursively(IfClause ifClause,
                                          List<IfClause> altIfClauses,
                                          int altIfClauseNext,
                                          SeqLang elseSeq,
                                          Ident exprIdent,
                                          LocalTarget target) throws Exception
    {
        LocalTarget boolTarget = target.asExprTargetWithSameScope();
        CompleteOrIdent boolIdent = ifClause.condition.accept(this, boolTarget);
        LocalTarget conTarget;
        if (exprIdent != null) {
            conTarget = target.asExprTargetWithNewScope(exprIdent);
        } else {
            conTarget = target.asStmtTargetWithNewScope();
        }
        ifClause.body.accept(this, conTarget);
        Instr conInstr = conTarget.build();
        Instr altInstr = null;
        if (altIfClauseNext < altIfClauses.size()) {
            LocalTarget altTarget;
            if (exprIdent != null) {
                altTarget = target.asExprTargetWithNewScope(exprIdent);
            } else {
                altTarget = target.asStmtTargetWithNewScope();
            }
            buildIfInstrsRecursively(altIfClauses.get(altIfClauseNext), altIfClauses, altIfClauseNext + 1,
                elseSeq, exprIdent, altTarget);
            altInstr = altTarget.build();
        } else if (elseSeq != null) {
            LocalTarget altTarget;
            if (exprIdent != null) {
                altTarget = target.asExprTargetWithNewScope(exprIdent);
            } else {
                altTarget = target.asStmtTargetWithNewScope();
            }
            elseSeq.accept(this, altTarget);
            altInstr = altTarget.build();
        }
        if (altInstr != null) {
            target.addInstr(new IfElseInstr(boolIdent, conInstr, altInstr, ifClause));
        } else {
            target.addInstr(new IfInstr(boolIdent, conInstr, ifClause));
        }
    }

    private void buildMatchClauseWithGuard(MatchClause matchClause, boolean elseNeeded, SourceSpan elseSpan, Ident exprIdent,
                                           LocalTarget caseBodyTarget)
        throws Exception
    {
        // BUILD IF BODY INSTR

        StmtOrExpr guard = matchClause.guard;
        Ident guardIdent = allocateNextSystemVarIdent();
        caseBodyTarget.addIdentDef(new IdentDef(guardIdent));
        LocalTarget guardTarget = caseBodyTarget.asExprTargetWithSameScope(guardIdent);
        guard.accept(this, guardTarget);
        LocalTarget ifBodyTarget;
        if (exprIdent != null) {
            ifBodyTarget = caseBodyTarget.asExprTargetWithNewScope(exprIdent);
        } else {
            ifBodyTarget = caseBodyTarget.asStmtTargetWithNewScope();
        }
        matchClause.accept(this, ifBodyTarget);
        Instr ifBodyInstr = ifBodyTarget.build();

        // CREATE IF INSTR

        Instr ifInstr;
        if (elseNeeded) {
            Instr applyElseInstr;
            if (exprIdent != null) {
                applyElseInstr = new ApplyInstr(Ident.$ELSE, List.of(exprIdent), elseSpan);
            } else {
                applyElseInstr = new ApplyInstr(Ident.$ELSE, List.of(), elseSpan);
            }
            ifInstr = new IfElseInstr(guardIdent, ifBodyInstr, applyElseInstr, guard);
        } else {
            ifInstr = new IfInstr(guardIdent, ifBodyInstr, guard);
        }
        caseBodyTarget.addInstr(ifInstr);
    }

    private ProcDef buildProcDef(List<Pat> params, Ident returnParam, List<StmtOrExpr> bodyList, SourceSpan sourceSpan)
        throws Exception
    {
        List<Ident> xs = new ArrayList<>(params.size() + 1);
        compileParamsToIdents(params, xs);
        LocalTarget bodyTarget;
        if (returnParam != null) {
            xs.add(returnParam);
            bodyTarget = LocalTarget.createExprTargetForFuncBody(returnParam);
        } else {
            bodyTarget = LocalTarget.createStmtTargetForProcBody();
        }
        buildBodyInstrs(bodyList, bodyTarget);
        if (bodyTarget.isReturnUsed()) {
            bodyTarget.addInstr(new JumpCatchInstr(RETURN_ID, sourceSpan.toSourceEnd()));
        }
        Instr bodyInstr = bodyTarget.build();
        return new ProcDef(xs, bodyInstr, sourceSpan);
    }

    final Ident toIdentOrNextAnonymousIdent(Ident ident) {
        return ident.isAnonymous() ? allocateNextSystemAnonymousIdent() : ident;
    }

    @Override
    public final CompleteOrIdent visitActExpr(ActExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope(exprIdent);
        lang.seq.accept(this, childTarget);
        Instr actBodyInstr = childTarget.build();
        target.addInstr(new ActInstr(actBodyInstr, exprIdent, lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitActorExpr(ActorExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        buildActorInstrs(exprIdent, lang, target);
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitActorStmt(ActorStmt lang, LocalTarget target) throws Exception {
        target.addIdentDef(new IdentDef(lang.name.ident));
        buildActorInstrs(lang.name.ident, lang, target);
        return null;
    }

    @Override
    public final CompleteOrIdent visitAndExpr(AndExpr lang, LocalTarget target) throws Exception {

        // Translate:
        //     z = k > 5 && k < 11
        //
        // Into:
        //     local $v0 in
        //        $gt(k, 5, $v0)
        //        if $v0 then
        //            $lt(k, 11, z)
        //        else
        //            $bind(false, z)
        //        end
        //    end

        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);

        LocalTarget rightTarget = target.asExprTargetWithNewScope(exprIdent);
        CompleteOrIdent arg2Bool = lang.arg2.accept(this, rightTarget);
        if (rightTarget.offeredIdent() != null) {
            rightTarget.addInstr(BindInstr.create(exprIdent, arg2Bool, lang.arg2));
        }
        Instr arg2Instr = rightTarget.build();

        LocalTarget leftTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg1Bool = lang.arg1.accept(this, leftTarget);
        BindInstr arg1False = BindInstr.create(exprIdent, Bool.FALSE, lang.arg1);
        leftTarget.addInstr(new IfElseInstr(arg1Bool, arg2Instr, arg1False, lang));

        target.addInstr(leftTarget.build());

        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitAnyType(AnyType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitApplyLang(ApplyLang lang, LocalTarget target) throws Exception {
        Ident exprIdent = target.isExprTarget() ? acceptOfferedIdentOrNextSystemVarIdent(target) : null;
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent proc = lang.proc.accept(this, childTarget);
        List<CompleteOrIdent> ys = new ArrayList<>();
        for (StmtOrExpr arg : lang.args) {
            ys.add(arg.accept(this, childTarget));
        }
        if (exprIdent != null) {
            ys.add(exprIdent);
        }
        childTarget.addInstr(new ApplyInstr(proc, ys, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitArrayType(ArrayType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitAskStmt(AskStmt lang, LocalTarget target) throws Exception {
        Ident exprIdent = allocateNextSystemVarIdent();
        LocalTarget askTarget = target.asAskTargetWithNewScope(exprIdent);
        askTarget.addIdentDef(new IdentDef(exprIdent));
        lang.body.accept(this, askTarget);
        askTarget.addInstr(new ApplyInstr(Ident.$RESPOND, List.of(exprIdent), lang.toSourceEnd()));
        target.addInstr(askTarget.build());
        return null;
    }

    @Override
    public final CompleteOrIdent visitBeginLang(BeginLang lang, LocalTarget target) throws Exception {
        return buildBodyInstrs(lang.body.list, target);
    }

    @Override
    public final CompleteOrIdent visitBoolAsExpr(BoolAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.bool;
        }
        target.addInstr(BindInstr.create(exprIdent, lang.bool, lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitBoolAsPat(BoolAsPat lang, LocalTarget target) {
        throw new IllegalStateException("BoolAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitBoolAsType(BoolAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitBoolType(BoolType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitBreakStmt(BreakStmt lang, LocalTarget target) {
        if (!target.isBreakAllowed()) {
            throw new BreakNotAllowedError(lang);
        }
        target.setBreakUsed();
        target.addInstr(new JumpThrowInstr(BREAK_ID, lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitCaseClause(CaseClause lang, LocalTarget target) throws Exception {
        lang.body.accept(this, target);
        return null;
    }

    @Override
    public final CompleteOrIdent visitCaseLang(CaseLang lang, LocalTarget target) throws Exception {
        Ident exprIdent = target.isExprTarget() ? acceptOfferedIdentOrNextSystemVarIdent(target) : null;
        CompleteOrIdent arg = lang.arg.accept(this, target);
        visitMatchClauses(arg, lang.caseClause, lang.altCaseClauses, 0, lang.elseSeq, exprIdent, target);
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitCatchClause(CatchClause lang, LocalTarget target) throws Exception {
        lang.body.accept(this, target);
        return null;
    }

    @Override
    public final CompleteOrIdent visitCharAsExpr(CharAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.value();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.value(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitCharAsType(CharAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitCharType(CharType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitContinueStmt(ContinueStmt lang, LocalTarget target) {
        if (!target.isContinueAllowed()) {
            throw new ContinueNotAllowedError(lang);
        }
        target.setContinueUsed();
        target.addInstr(new JumpThrowInstr(CONTINUE_ID, lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitDec128AsExpr(Dec128AsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.dec128();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.dec128(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitDec128AsType(Dec128AsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitDec128Type(Dec128Type lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitDotSelectExpr(DotSelectExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent rec = lang.recExpr.accept(this, childTarget);
        FeatureOrIdent feature;
        if (lang.featureExpr instanceof IdentAsExpr identAsExpr) {
            feature = Str.of(identAsExpr.ident.name);
        } else {
            feature = (FeatureOrIdent) lang.featureExpr.accept(this, childTarget);
        }
        childTarget.addInstr(new SelectInstr(rec, feature, exprIdent, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitEofAsExpr(EofAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.value();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.value(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitEofAsPat(EofAsPat lang, LocalTarget target) {
        throw new IllegalStateException("EofAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitEofAsType(EofAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitEofType(EofType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFieldExpr(FieldExpr lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFieldPat(FieldPat lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFieldType(FieldType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFlt32AsType(Flt32AsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFlt32Type(Flt32Type lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFlt64AsExpr(Flt64AsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.flt64();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.flt64(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitFlt64AsType(Flt64AsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitFlt64Type(Flt64Type lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitForStmt(ForStmt lang, LocalTarget target) throws Exception {

        LocalTarget childTarget = target.asStmtTargetWithNewScope();

        // ITER

        childTarget.addIdentDef(new IdentDef(Ident.$ITER));
        LocalTarget iterTarget = childTarget.asExprTargetWithSameScope(Ident.$ITER);
        lang.iter.accept(this, iterTarget);

        // FOR

        childTarget.addIdentDef(new IdentDef(Ident.$FOR));
        LocalTarget forTarget = childTarget.asStmtTargetWithNewScope();
        IdentAsPat forNextAsPat = assertIdentAsPatNotEscaped(lang.pat);
        Ident forNext = forNextAsPat.ident;
        forTarget.addIdentDef(new IdentDef(forNext));
        forTarget.addInstr(new ApplyInstr(Ident.$ITER, List.of(forNext), lang.iter));
        Ident forBool = allocateNextSystemVarIdent();
        forTarget.addIdentDef(new IdentDef(forBool));
        forTarget.addInstr(new DisentailsInstr(forNext, Eof.SINGLETON, forBool, lang.iter));
        LocalTarget forBodyTarget = childTarget.asStmtTargetForLoopBodyWithNewScope();
        lang.body.accept(this, forBodyTarget);
        if (forBodyTarget.isContinueUsed()) {
            forBodyTarget.addInstr(new JumpCatchInstr(CONTINUE_ID, lang.body.toSourceEnd()));
        }
        forBodyTarget.addInstr(new ApplyInstr(Ident.$FOR, List.of(), lang.body.toSourceEnd()));
        forTarget.addInstr(new IfInstr(forBool, forBodyTarget.build(), lang.body));
        Instr forInstr = forTarget.build();
        childTarget.addInstr(new CreateProcInstr(
            Ident.$FOR,
            new ProcDef(List.of(), forInstr, lang.body),
            lang.body));

        // FIRST INVOCATION

        childTarget.addInstr(new ApplyInstr(Ident.$FOR, List.of(), lang.body.toSourceEnd()));
        if (forBodyTarget.isBreakUsed()) {
            childTarget.addInstr(new JumpCatchInstr(BREAK_ID, lang.body.toSourceEnd()));
        }

        target.addInstr(childTarget.build());
        return null;
    }

    @Override
    public final CompleteOrIdent visitFuncExpr(FuncExpr lang, LocalTarget target) throws Exception {
        Ident funcIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        ProcDef procDef = buildProcDef(lang.params, Ident.$R, lang.body.list, lang);
        target.addInstr(new CreateProcInstr(funcIdent, procDef, lang));
        return funcIdent;
    }

    @Override
    public final CompleteOrIdent visitFuncStmt(FuncStmt lang, LocalTarget target) throws Exception {
        target.addIdentDef(new IdentDef(lang.name.ident));
        ProcDef procDef = buildProcDef(lang.params, Ident.$R, lang.body.list, lang);
        target.addInstr(new CreateProcInstr(lang.name.ident, procDef, lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitFuncType(FuncType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitGroupExpr(GroupExpr lang, LocalTarget target) throws Exception {
        return lang.expr.accept(this, target);
    }

    @Override
    public final CompleteOrIdent visitIdentAsExpr(IdentAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.ident;
        }
        target.addInstr(BindInstr.create(exprIdent, lang.ident, lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitIdentAsPat(IdentAsPat lang, LocalTarget target) {
        throw new IllegalStateException("IdentAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitIdentAsProtocol(IdentAsProtocol lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitIdentAsType(IdentAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitIdentVarDecl(IdentVarDecl lang, LocalTarget target) {
        IdentAsPat identAsPat = lang.identAsPat;
        if (identAsPat.escaped) {
            throw new InvalidEscapeError(identAsPat);
        }
        target.addIdentDef(new IdentDef(identAsPat.ident));
        return null;
    }

    @Override
    public final CompleteOrIdent visitIfClause(IfClause lang, LocalTarget target) {
        throw new IllegalStateException("IfClause visited directly");
    }

    @Override
    public final CompleteOrIdent visitIfLang(IfLang lang, LocalTarget target) throws Exception {
        Ident exprIdent = target.isExprTarget() ? acceptOfferedIdentOrNextSystemVarIdent(target) : null;
        buildIfInstrsRecursively(lang.ifClause, lang.altIfClauses, 0, lang.elseSeq, exprIdent, target);
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitImportName(ImportName lang, LocalTarget target) {
        throw new IllegalStateException("ImportName visited directly");
    }

    @Override
    public final CompleteOrIdent visitImportStmt(ImportStmt lang, LocalTarget target) {
        LocalTarget childTarget = target.asStmtTargetWithNewScope();
        List<CompleteOrIdent> ys = new ArrayList<>();
        StringBuilder qualifier = new StringBuilder();
        for (int i = 0; i < lang.qualifier.size(); i++) {
            if (i > 0) {
                qualifier.append(".");
            }
            qualifier.append(lang.qualifier.get(i));
        }
        ys.add(Str.of(qualifier.toString()));
        CompleteTupleBuilder builder = Rec.completeTupleBuilder();
        // Imported names are added to the parent scope, not the child scope
        for (ImportName in : lang.names) {
            if (in.alias != null) {
                target.addIdentDef(new IdentDef(in.alias.ident));
                builder.addValue(Rec.completeTupleBuilder()
                    .addValue(Str.of(in.name.ident.name))
                    .addValue(Str.of(in.alias.ident.name))
                    .build());
            } else {
                target.addIdentDef(new IdentDef(in.name.ident));
                builder.addValue(Str.of(in.name.ident.name));
            }
        }
        ys.add(builder.build());
        childTarget.addInstr(new ApplyInstr(Ident.$IMPORT, ys, lang));
        target.addInstr(childTarget.build());
        return null;
    }

    @Override
    public final CompleteOrIdent visitIndexSelectExpr(IndexSelectExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent rec = lang.recExpr.accept(this, childTarget);
        FeatureOrIdent feature = (FeatureOrIdent) lang.featureExpr.accept(this, childTarget);
        childTarget.addInstr(new SelectInstr(rec, feature, exprIdent, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitIntersectionProtocol(IntersectionProtocol lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitIntersectionType(IntersectionType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitInitVarDecl(InitVarDecl lang, LocalTarget target) throws Exception {
        // TODO: Allow patterns on the LHS when the Validator with strong type checking is available
        // Currently, we only allow identifiers on the left side of an initialization. Allowing a pattern on the left
        // side would be syntactic sugar for a limited case instruction. For now, using a full-featured case
        // instruction allows the programmer to explicitly handle mismatches. With type checking, we can ensure
        // that the RHS will deconstruct into the LHS.
        IdentAsPat identAsPat = assertIdentAsPatNotEscaped(lang.varPat);
        // Optimize simple value assignments
        if (lang.valueExpr instanceof ScalarAsExpr scalarAsExpr) {
            target.addIdentDef(new IdentDef(identAsPat.ident, scalarAsExpr.value()));
        } else {
            target.addIdentDef(new IdentDef(identAsPat.ident));
            LocalTarget rightSideTarget = target.asExprTargetWithSameScope(identAsPat.ident);
            lang.valueExpr.accept(this, rightSideTarget);
        }
        return null;
    }

    @Override
    public final CompleteOrIdent visitInt32AsType(Int32AsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitInt32Type(Int32Type lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitInt64AsExpr(Int64AsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.int64();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.int64(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitInt64AsPat(Int64AsPat lang, LocalTarget target) {
        throw new IllegalStateException("IntAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitInt64AsType(Int64AsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitInt64Type(Int64Type lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitLocalLang(LocalLang lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        LocalTarget childTarget;
        if (exprIdent != null) {
            childTarget = target.asExprTargetWithNewScope(exprIdent);
        } else {
            childTarget = target.asStmtTargetWithNewScope();
        }
        for (VarDecl d : lang.varDecls) {
            d.accept(this, childTarget);
        }
        buildBodyInstrs(lang.body.list, childTarget);
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    private <T extends MatchClause> void visitMatchClauses(CompleteOrIdent arg,
                                                           T matchClause,
                                                           List<T> altMatchClauses,
                                                           int altMatchClauseNext,
                                                           SeqLang elseSeq,
                                                           Ident exprIdent,
                                                           LocalTarget target) throws Exception
    {
        //////////////////////////////////////////////////////
        // TODO: FIX SOURCE RANGES -- THEY ARE CERTAINLY WRONG
        //////////////////////////////////////////////////////

        LocalTarget childTarget;
        if (exprIdent != null) {
            childTarget = target.asExprTargetWithNewScope(exprIdent);
        } else {
            childTarget = target.asStmtTargetWithNewScope();
        }

        // CREATE ELSE PROC

        boolean elseNeeded = altMatchClauseNext < altMatchClauses.size() || elseSeq != null;
        if (elseNeeded) {
            childTarget.addIdentDef(new IdentDef(Ident.$ELSE));
            LocalTarget elseOrAltTarget;
            if (exprIdent != null) {
                elseOrAltTarget = childTarget.asExprTargetWithNewScope(Ident.$R);
            } else {
                elseOrAltTarget = childTarget.asStmtTargetWithNewScope();
            }
            if (altMatchClauseNext < altMatchClauses.size()) {
                visitMatchClauses(arg, altMatchClauses.get(altMatchClauseNext), altMatchClauses,
                    altMatchClauseNext + 1, elseSeq, Ident.$R, elseOrAltTarget);
            } else {
                elseSeq.accept(this, elseOrAltTarget);
            }
            Instr elseInstr = elseOrAltTarget.build();
            List<Ident> elseParams;
            if (exprIdent != null) {
                elseParams = List.of(Ident.$R);
            } else {
                elseParams = List.of();
            }
            childTarget.addInstr(new CreateProcInstr(
                Ident.$ELSE, new ProcDef(elseParams, elseInstr, elseInstr),
                elseInstr));
        }

        // COMPILE PATTERN

        CompiledPat cp = new CompiledPat(matchClause.pat, this);
        cp.compile();

        // CREATE CASE INSTR

        childTarget.addInstr(buildCaseInstrs(arg, cp.root(), cp.children(), 0, matchClause,
            elseNeeded, elseSeq, exprIdent, childTarget));

        target.addInstr(childTarget.build());
    }

    @Override
    public final CompleteOrIdent visitMetaField(MetaField lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitMetaRec(MetaRec lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitMetaTuple(MetaTuple lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitModuleStmt(ModuleStmt lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitNewExpr(NewExpr lang, LocalTarget target) throws Exception {
        TypeApply typeApply = lang.typeApply;
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent cls = typeApply.name.typeIdent();
        List<FeatureOrIdent> path = List.of(Str.of("new"));
        List<CompleteOrIdent> ys = new ArrayList<>();
        for (StmtOrExpr arg : lang.args) {
            ys.add(arg.accept(this, childTarget));
        }
        ys.add(exprIdent);
        childTarget.addInstr(new SelectAndApplyInstr(cls, path, ys, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitNullAsExpr(NullAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.value();
        }
        target.addInstr(BindInstr.create(exprIdent, lang.value(), lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitNullAsPat(NullAsPat lang, LocalTarget target) {
        throw new IllegalStateException("NullAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitNullAsType(NullAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitNullType(NullType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitOrExpr(OrExpr lang, LocalTarget target) throws Exception {

        // Translate:
        //     z = k < 5 || k > 11
        //
        // Into:
        //     local $v0 in
        //        $lt(k, 5, $v0)
        //        if $v0 then
        //            $bind(true, z)
        //        else
        //            $gt(k, 11, z)
        //        end
        //    end

        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);

        LocalTarget rightTarget = target.asExprTargetWithNewScope(exprIdent);
        CompleteOrIdent arg2Bool = lang.arg2.accept(this, rightTarget);
        if (rightTarget.offeredIdent() != null) {
            rightTarget.addInstr(BindInstr.create(exprIdent, arg2Bool, lang.arg2));
        }
        Instr arg2Instr = rightTarget.build();

        LocalTarget leftTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg1Bool = lang.arg1.accept(this, leftTarget);
        BindInstr arg1True = BindInstr.create(exprIdent, Bool.TRUE, lang.arg1);
        leftTarget.addInstr(new IfElseInstr(arg1Bool, arg1True, arg2Instr, lang));

        target.addInstr(leftTarget.build());

        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitPackageStmt(PackageStmt lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProcExpr(ProcExpr lang, LocalTarget target) throws Exception {
        Ident procIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        ProcDef procDef = buildProcDef(lang.params, null, lang.body.list, lang);
        target.addInstr(new CreateProcInstr(procIdent, procDef, lang));
        return procIdent;
    }

    @Override
    public final CompleteOrIdent visitProcStmt(ProcStmt lang, LocalTarget target) throws Exception {
        target.addIdentDef(new IdentDef(lang.name.ident));
        ProcDef procDef = buildProcDef(lang.params, null, lang.body.list, lang);
        target.addInstr(new CreateProcInstr(lang.name.ident, procDef, lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitProcType(ProcType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProductExpr(ProductExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg1 = lang.arg1.accept(this, childTarget);
        CompleteOrIdent arg2 = lang.arg2.accept(this, childTarget);
        Instr productInstr;
        if (lang.oper == ProductOper.MULTIPLY) {
            productInstr = new MultiplyInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == ProductOper.DIVIDE) {
            productInstr = new DivideInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == ProductOper.MODULO) {
            productInstr = new ModuloInstr(arg1, arg2, exprIdent, lang);
        } else {
            // This condition should never execute
            throw new IllegalArgumentException("Product operator not recognized");
        }
        childTarget.addInstr(productInstr);
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitProtocolApply(ProtocolApply lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProtocolAskHandler(ProtocolAskHandler lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProtocolStmt(ProtocolStmt lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProtocolStreamHandler(ProtocolStreamHandler lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProtocolStruct(ProtocolStruct lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitProtocolTellHandler(ProtocolTellHandler lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitRecExpr(RecExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteRec completeRec = lang.checkComplete();
        if (completeRec != null) {
            childTarget.addInstr(BindInstr.create(exprIdent, completeRec, lang));
        } else {
            LocalTarget recTarget = childTarget.asExprTargetWithNewScope();
            LiteralOrIdent label;
            if (lang.label() == null) {
                label = Rec.DEFAULT_LABEL;
            } else {
                label = (LiteralOrIdent) lang.label().accept(this, recTarget);
            }
            List<FieldDef> fieldDefs = new ArrayList<>(lang.fields().size());
            for (FieldExpr f : lang.fields()) {
                FeatureOrIdent feature = (FeatureOrIdent) f.feature.accept(this, recTarget);
                CompleteOrIdent value = f.value.accept(this, recTarget);
                fieldDefs.add(new FieldDef(feature, value, f));
            }
            RecDef recDef = new RecDef(label, fieldDefs, lang);
            recTarget.addInstr(new CreateRecInstr(exprIdent, recDef, lang));
            childTarget.addInstr(recTarget.build());
        }
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitRecPat(RecPat lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitRecType(RecType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitRecTypeExpr(RecTypeExpr lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitRelationalExpr(RelationalExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg1 = lang.arg1.accept(this, childTarget);
        CompleteOrIdent arg2 = lang.arg2.accept(this, childTarget);
        Instr relInstr;
        if (lang.oper == RelationalOper.EQUAL_TO) {
            relInstr = new EntailsInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == RelationalOper.NOT_EQUAL_TO) {
            relInstr = new DisentailsInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == RelationalOper.LESS_THAN) {
            relInstr = new LessThanInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == RelationalOper.LESS_THAN_OR_EQUAL_TO) {
            relInstr = new LessThanOrEqualToInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == RelationalOper.GREATER_THAN) {
            relInstr = new GreaterThanInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == RelationalOper.GREATER_THAN_OR_EQUAL_TO) {
            relInstr = new GreaterThanOrEqualToInstr(arg1, arg2, exprIdent, lang);
        } else {
            // This condition should never execute
            throw new IllegalArgumentException("Relational operator not recognized");
        }
        childTarget.addInstr(relInstr);
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitRespondStmt(RespondStmt lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitReturnStmt(ReturnStmt lang, LocalTarget target) throws Exception {
        if (!target.isReturnAllowed()) {
            throw new ReturnNotAllowedError(lang);
        }
        if (lang.value != null) {
            lang.value.accept(this, target.asExprTargetWithSameScope(Ident.$R));
        }
        target.setReturnUsed();
        target.addInstr(new JumpThrowInstr(RETURN_ID, lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitSelectAndApplyLang(SelectAndApplyLang lang, LocalTarget target)
        throws Exception
    {
        Ident exprIdent = target.isExprTarget() ? acceptOfferedIdentOrNextSystemVarIdent(target) : null;
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent rec = null;
        List<FeatureOrIdent> path = new ArrayList<>();
        SelectExpr selectExpr = lang.selectExpr;
        while (selectExpr != null) {
            FeatureOrIdent nestedFeature;
            if ((selectExpr instanceof DotSelectExpr) && (selectExpr.featureExpr instanceof IdentAsExpr identAsExpr)) {
                nestedFeature = Str.of(identAsExpr.ident.name);
            } else {
                nestedFeature = (FeatureOrIdent) selectExpr.featureExpr.accept(this, childTarget);
            }
            path.add(0, nestedFeature);
            if (selectExpr.recExpr instanceof SelectExpr nextSelectExpr) {
                selectExpr = nextSelectExpr;
            } else {
                rec = selectExpr.recExpr.accept(this, childTarget);
                selectExpr = null;
            }
        }
        List<CompleteOrIdent> ys = new ArrayList<>();
        for (int i = 0; i < lang.args.size(); i++) {
            ys.add(lang.args.get(i).accept(this, childTarget));
        }
        if (exprIdent != null) {
            ys.add(exprIdent);
        }
        childTarget.addInstr(new SelectAndApplyInstr(rec, path, ys, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitSeqLang(SeqLang lang, LocalTarget target) throws Exception {
        return buildBodyInstrs(lang.list, target);
    }

    @Override
    public final CompleteOrIdent visitSetCellValueStmt(SetCellValueStmt lang, LocalTarget target) throws Exception {
        LocalTarget childTarget = target.asStmtTargetWithNewScope();
        CompleteOrIdent leftSide = lang.leftSide.accept(this, childTarget);
        if (!(leftSide instanceof Ident leftSideIdent)) {
            throw new NotIdentError(lang.leftSide);
        }
        LocalTarget rightSideTarget = childTarget.asExprTargetWithSameScope();
        CompleteOrIdent rightSide = lang.rightSide.accept(this, rightSideTarget);
        childTarget.addInstr(new SetCellValueInstr(leftSideIdent, rightSide, lang));
        target.addInstr(childTarget.build());
        return null;
    }

    @Override
    public final CompleteOrIdent visitSkipStmt(SkipStmt lang, LocalTarget target) {
        target.addInstr(new SkipInstr(lang));
        return null;
    }

    @Override
    public final CompleteOrIdent visitSpawnExpr(SpawnExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = target.isExprTarget() ? acceptOfferedIdentOrNextSystemVarIdent(target) : null;
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        List<CompleteOrIdent> ys = new ArrayList<>();
        for (StmtOrExpr arg : lang.args) {
            ys.add(arg.accept(this, childTarget));
        }
        if (exprIdent != null) {
            ys.add(exprIdent);
        }
        childTarget.addInstr(new ApplyInstr(Ident.$SPAWN, ys, lang));
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitStrAsExpr(StrAsExpr lang, LocalTarget target) {
        Ident exprIdent = acceptOfferedIdentOrNull(target);
        if (exprIdent == null) {
            return lang.str;
        }
        target.addInstr(BindInstr.create(exprIdent, lang.str, lang));
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitStrAsPat(StrAsPat lang, LocalTarget target) {
        throw new IllegalStateException("StrAsPat visited directly");
    }

    @Override
    public final CompleteOrIdent visitStrAsType(StrAsType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitStrType(StrType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitSumExpr(SumExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg1 = lang.arg1.accept(this, childTarget);
        CompleteOrIdent arg2 = lang.arg2.accept(this, childTarget);
        Instr sumInstr;
        if (lang.oper == SumOper.ADD) {
            sumInstr = new AddInstr(arg1, arg2, exprIdent, lang);
        } else if (lang.oper == SumOper.SUBTRACT) {
            sumInstr = new SubtractInstr(arg1, arg2, exprIdent, lang);
        } else {
            // This condition should never execute
            throw new IllegalArgumentException("Sum operator not recognized");
        }
        childTarget.addInstr(sumInstr);
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitTellStmt(TellStmt lang, LocalTarget target) throws Exception {
        lang.body.accept(this, target);
        return null;
    }

    @Override
    public final CompleteOrIdent visitThrowLang(ThrowLang lang, LocalTarget target) throws Exception {
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg = lang.arg.accept(this, childTarget);
        childTarget.addInstr(new ThrowInstr(arg, lang));
        target.addInstr(childTarget.build());
        return null;
    }

    @Override
    public final CompleteOrIdent visitTokenType(TokenType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTryLang(TryLang lang, LocalTarget target) throws Exception {

        Ident exprIdent = acceptOfferedIdentOrNull(target);

        LocalTarget childTarget;
        if (exprIdent != null) {
            childTarget = target.asExprTargetWithNewScope(exprIdent);
        } else {
            childTarget = target.asStmtTargetWithNewScope();
        }

        //////////////////////////////////////////////////////
        // TODO: FIX SOURCE RANGES -- THEY ARE CERTAINLY WRONG
        //////////////////////////////////////////////////////

        // FINALLY

        SeqLang finallySeq = lang.finallySeq;
        if (finallySeq != null) {
            childTarget.addIdentDef(new IdentDef(Ident.$FINALLY));
            LocalTarget finallyTarget = LocalTarget.createStmtTargetForFinally();
            finallySeq.accept(this, finallyTarget);
            Instr finallyInstr = finallyTarget.build();
            childTarget.addInstr(new CreateProcInstr(Ident.$FINALLY, new ProcDef(List.of(), finallyInstr, finallySeq), finallySeq));
        }

        // TRY/CATCH

        LocalTarget tryBodyTarget;
        if (exprIdent != null) {
            tryBodyTarget = childTarget.asExprTargetWithNewScope(exprIdent);
        } else {
            tryBodyTarget = childTarget.asStmtTargetWithNewScope();
        }
        lang.body.accept(this, tryBodyTarget);
        Instr tryBodyInstr = tryBodyTarget.build();
        Ident catchIdent = allocateNextSystemVarIdent();
        LocalTarget catchBodyTarget;
        if (exprIdent != null) {
            catchBodyTarget = childTarget.asExprTargetWithNewScope(exprIdent);
        } else {
            catchBodyTarget = childTarget.asStmtTargetWithNewScope();
        }
        SourceSpan endOfTrySpan = lang.toSourceEnd();
        ApplyLang applyFinallyLang = new ApplyLang(new IdentAsExpr(Ident.$FINALLY, endOfTrySpan),
            List.of(), endOfTrySpan);
        ThrowLang throwAgainLang = new ThrowLang(new IdentAsExpr(catchIdent, endOfTrySpan), endOfTrySpan);
        SeqLang elseSeq;
        if (finallySeq != null) {
            elseSeq = new SeqLang(List.of(applyFinallyLang, throwAgainLang), endOfTrySpan);
        } else {
            elseSeq = new SeqLang(List.of(throwAgainLang), endOfTrySpan);
        }
        visitMatchClauses(catchIdent, lang.catchClauses.get(0), lang.catchClauses, 1,
            elseSeq, exprIdent, catchBodyTarget);
        Instr catchBodyInstr = catchBodyTarget.build();
        SourceSpan catchSpan = SourceSpan.adjoin(lang.catchClauses);
        childTarget.addInstr(new TryInstr(tryBodyInstr, new CatchInstr(catchIdent, catchBodyInstr, catchSpan), catchSpan));

        // APPLY FINALLY

        if (finallySeq != null) {
            childTarget.addInstr(new ApplyInstr(Ident.$FINALLY, List.of(), finallySeq));
        }

        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitTupleExpr(TupleExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteTuple completeTuple = lang.checkComplete();
        if (completeTuple != null) {
            childTarget.addInstr(BindInstr.create(exprIdent, completeTuple, lang));
        } else {
            LocalTarget recTarget = childTarget.asExprTargetWithNewScope();
            LiteralOrIdent label;
            if (lang.label() == null) {
                label = Rec.DEFAULT_LABEL;
            } else {
                label = (LiteralOrIdent) lang.label().accept(this, recTarget);
            }
            List<ValueDef> valueDefs = new ArrayList<>(lang.values().size());
            for (StmtOrExpr v : lang.values()) {
                CompleteOrIdent value = v.accept(this, recTarget);
                valueDefs.add(new ValueDef(value, v));
            }
            TupleDef tupleDef = new TupleDef(label, valueDefs, lang);
            recTarget.addInstr(new CreateTupleInstr(exprIdent, tupleDef, lang));
            childTarget.addInstr(recTarget.build());
        }
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitTuplePat(TuplePat lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTupleType(TupleType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTupleTypeExpr(TupleTypeExpr lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTypeApply(TypeApply lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTypeParam(TypeParam lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitTypeStmt(TypeStmt lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitUnaryExpr(UnaryExpr lang, LocalTarget target) throws Exception {
        Ident exprIdent = acceptOfferedIdentOrNextSystemVarIdent(target);
        LocalTarget childTarget = target.asExprTargetWithNewScope();
        CompleteOrIdent arg = lang.arg.accept(this, childTarget);
        Instr unaryInstr;
        if (lang.oper == UnaryOper.NOT) {
            unaryInstr = new NotInstr(arg, exprIdent, lang);
        } else if (lang.oper == UnaryOper.ACCESS) {
            if (!(arg instanceof Ident arg1Ident)) {
                throw new NotIdentError(lang.arg);
            }
            unaryInstr = new GetCellValueInstr(arg1Ident, exprIdent, lang);
        } else if (lang.oper == UnaryOper.NEGATE) {
            unaryInstr = new NegateInstr(arg, exprIdent, lang);
        } else {
            // This condition should never execute
            throw new IllegalArgumentException("Unary operator not recognized");
        }
        childTarget.addInstr(unaryInstr);
        target.addInstr(childTarget.build());
        return exprIdent;
    }

    @Override
    public final CompleteOrIdent visitUnifyStmt(UnifyStmt lang, LocalTarget target) throws Exception {
        CompleteOrIdent leftSide = lang.leftSide.accept(this, target);
        if (leftSide instanceof Ident leftSideIdent) {
            LocalTarget rightSideTarget = target.asExprTargetWithSameScope(leftSideIdent);
            CompleteOrIdent rightSide = lang.rightSide.accept(this, rightSideTarget);
            if (rightSideTarget.offeredIdent() != null) {
                // The offered identifier was not consumed, so we must add a bind instruction here
                target.addInstr(BindInstr.create(leftSide, rightSide, lang));
            }
        } else {
            CompleteOrIdent rightSide = lang.rightSide.accept(this, target);
            target.addInstr(BindInstr.create(leftSide, rightSide, lang));
        }
        return null;
    }

    @Override
    public final CompleteOrIdent visitUnionType(UnionType lang, LocalTarget target) {
        throw new NeedsImpl();
    }

    @Override
    public final CompleteOrIdent visitVarStmt(VarStmt lang, LocalTarget target) throws Exception {
        for (VarDecl next : lang.varDecls) {
            next.accept(this, target);
        }
        return null;
    }

    @Override
    public final CompleteOrIdent visitWhileStmt(WhileStmt lang, LocalTarget target) throws Exception {

        LocalTarget childTarget = target.asStmtTargetWithNewScope();

        // GUARD

        childTarget.addIdentDef(new IdentDef(Ident.$GUARD));
        LocalTarget guardTarget = childTarget.asExprTargetWithNewScope(Ident.$R);
        lang.cond.accept(this, guardTarget);
        Instr guardInstr = guardTarget.build();
        childTarget.addInstr(new CreateProcInstr(
            Ident.$GUARD,
            new ProcDef(List.of(Ident.$R), guardInstr, lang.cond),
            lang.cond));

        // WHILE

        childTarget.addIdentDef(new IdentDef(Ident.$WHILE));
        LocalTarget whileTarget = childTarget.asStmtTargetWithNewScope();
        Ident whileBool = allocateNextSystemVarIdent();
        whileTarget.addIdentDef(new IdentDef(whileBool));
        whileTarget.addInstr(new ApplyInstr(Ident.$GUARD, List.of(whileBool), lang.cond));
        LocalTarget whileBodyTarget = childTarget.asStmtTargetForLoopBodyWithNewScope();
        lang.body.accept(this, whileBodyTarget);
        if (whileBodyTarget.isContinueUsed()) {
            whileBodyTarget.addInstr(new JumpCatchInstr(CONTINUE_ID, lang.body.toSourceEnd()));
        }
        whileBodyTarget.addInstr(new ApplyInstr(Ident.$WHILE, List.of(), lang.body.toSourceEnd()));
        whileTarget.addInstr(new IfInstr(whileBool, whileBodyTarget.build(), lang.body));
        Instr whileInstr = whileTarget.build();
        childTarget.addInstr(new CreateProcInstr(
            Ident.$WHILE,
            new ProcDef(List.of(), whileInstr, lang.body),
            lang.body));

        // FIRST INVOCATION

        childTarget.addInstr(new ApplyInstr(Ident.$WHILE, List.of(), lang.body.toSourceEnd()));
        if (whileBodyTarget.isBreakUsed()) {
            childTarget.addInstr(new JumpCatchInstr(BREAK_ID, lang.body.toSourceEnd()));
        }

        target.addInstr(childTarget.build());
        return null;
    }

}
