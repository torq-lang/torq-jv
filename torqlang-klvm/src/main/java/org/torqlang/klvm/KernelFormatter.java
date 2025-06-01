/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.FormatterState;
import org.torqlang.util.NeedsImpl;

import java.io.StringWriter;
import java.util.*;

/*
 * Format kernel values as kernel "source" (we do not have a formal grammar for the kernel language).
 *
 * Instructions are functions of Stack, Environment, and Memory. Instructions that contain record and procedure
 * expressions actually contain definitions, such as RecDef and ProcDef, that drive the creation of values inside
 * memory. The KernelFormatter class is designed to handle the particulars of formatting values, such as circular
 * references.
 *
 * We can format kernel values from different roots:
 *     Compiled instructions
 *         Includes instructions, identifiers, definitions, and complete values
 *     Kernel memory values
 *         Includes values created by computed instructions, such as closures created by CreateProcInstr and
 *         records created by CreateRecInstr
 *     Machine state
 *         Includes the stack and environment
 *         Includes kernel memory variables (see above)
 *         Includes compiled instructions (see above)
 */
public final class KernelFormatter implements KernelVisitor<FormatterState, Void> {

    public static final KernelFormatter DEFAULT = new KernelFormatter();

    private static final String $ADD = "$add";
    private static final String $BIND = "$bind";
    private static final String $CREATE_ACTOR_CTOR = "$create_actor_ctor";
    private static final String $CREATE_PROC = "$create_proc";
    private static final String $CREATE_REC = "$create_rec";
    private static final String $CREATE_TUPLE = "$create_tuple";
    private static final String $DIV = "$div";
    private static final String $EQ = "$eq";
    private static final String $GE = "$ge";
    private static final String $GET = "$get";
    private static final String $GT = "$gt";
    private static final String $JUMP_CATCH = "$jump_catch";
    private static final String $JUMP_THROW = "$jump_throw";
    private static final String $LE = "$le";
    private static final String $LT = "$lt";
    private static final String $MOD = "$mod";
    private static final String $MULT = "$mult";
    private static final String $NE = "$ne";
    private static final String $NEGATE = "$negate";
    private static final String $NOT = "$not";
    private static final String $SET = "$set";
    private static final String $SUB = "$sub";
    private static final String $SELECT = "$select";
    private static final String $SELECT_APPLY = "$select_apply";

    private final int maxLevel;

    public KernelFormatter() {
        this(Integer.MAX_VALUE);
    }

    public KernelFormatter(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    private void accept(Kernel kernel, FormatterState state) throws Exception {
        if (state.level() != FormatterState.INLINE_VALUE && state.level() > maxLevel) {
            state.write("<<omitted>>");
        } else {
            kernel.accept(this, state);
        }
    }

    public final String format(Kernel kernel) {
        try (StringWriter sw = new StringWriter()) {
            FormatterState state = new FormatterState(sw);
            accept(kernel, state);
            state.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void formatApplyArgs(List<CompleteOrIdent> args, FormatterState state) throws Exception {
        state.write('(');
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                state.write(',');
                state.write(FormatterState.SPACE);
            }
            CompleteOrIdent y = args.get(i);
            accept(y, state.inline());
        }
        state.write(')');
    }

    private void formatBinaryInstr(String oper, CompleteOrIdent a, CompleteOrIdent b, Ident x, FormatterState state) throws Exception {
        state.write(oper);
        state.write('(');
        accept(a, state.inline());
        state.write(", ");
        accept(b, state.inline());
        state.write(", ");
        accept(x, state.inline());
        state.write(')');
    }

    private void formatBindInstr(Kernel a, Kernel x, FormatterState state) throws Exception {
        state.write($BIND);
        state.write('(');
        accept(a, state.inline());
        state.write(", ");
        accept(x, state.inline());
        state.write(')');
    }

    @Override
    public final Void visitActInstr(ActInstr instr, FormatterState state) throws Exception {
        state.write("$act");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.instr, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitActorCfg(ActorCfg value, FormatterState state) throws Exception {
        state.write("// ActorCfg:");
        state.writeNewLineAndIndent();
        List<Complete> args = value.args();
        if (args.isEmpty()) {
            state.write("// args.size() == 0");
            state.writeNewLineAndIndent();
        }
        for (int i = 0; i < args.size(); i++) {
            Complete arg = args.get(i);
            state.write("// arg[");
            state.write(Integer.toString(i));
            state.write("]: ");
            accept(arg, state.inline());
            state.writeNewLineAndIndent();
        }
        accept(value.handlersCtor(), state);
        return null;
    }

    @Override
    public final Void visitActorCtor(ActorCtor value, FormatterState state) throws Exception {
        return visitClosure(value.handlersCtor(), state);
    }

    @Override
    public final Void visitAddInstr(AddInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($ADD, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitApplyInstr(ApplyInstr instr, FormatterState state) throws Exception {
        accept(instr.x, state.inline());
        formatApplyArgs(instr.ys, state);
        return null;
    }

    @Override
    public final Void visitBindCompleteToCompleteInstr(BindCompleteToCompleteInstr instr, FormatterState state) throws Exception {
        formatBindInstr(instr.a, instr.x, state);
        return null;
    }

    @Override
    public final Void visitBindCompleteToIdentInstr(BindCompleteToIdentInstr instr, FormatterState state) throws Exception {
        formatBindInstr(instr.a, instr.x, state);
        return null;
    }

    @Override
    public final Void visitBindCompleteToValueOrVarInstr(BindCompleteToValueOrVarInstr instr, FormatterState state) throws Exception {
        formatBindInstr(instr.a, instr.x, state);
        return null;
    }

    @Override
    public final Void visitBindIdentToIdentInstr(BindIdentToIdentInstr instr, FormatterState state) throws Exception {
        formatBindInstr(instr.a, instr.x, state);
        return null;
    }

    @Override
    public final Void visitCaseElseInstr(CaseElseInstr instr, FormatterState state) throws Exception {
        state.write("case ");
        accept(instr.x, state.inline());
        state.write(" of ");
        accept(instr.valueOrPtn, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.consequent, nextLevelState);
        state.writeAfterNewLineAndIdent("else");
        nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.alternate, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitCaseInstr(CaseInstr instr, FormatterState state) throws Exception {
        state.write("case ");
        accept(instr.x, state.inline());
        state.write(" of ");
        accept(instr.valueOrPtn, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.consequent, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitCatchInstr(CatchInstr instr, FormatterState state) throws Exception {
        state.write("catch ");
        accept(instr.arg, state.inline());
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.caseInstr, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitClosure(Closure value, FormatterState state) throws Exception {
        if (value.capturedEnv().shallowSize() > 0) {
            visitEnv(value.capturedEnv(), state);
            state.writeNewLineAndIndent();
        }
        visitProcDef(value.procDef(), state);
        return null;
    }

    @Override
    public final Void visitCreateActorCtorInstr(CreateActorCtorInstr instr, FormatterState state) throws Exception {
        state.write($CREATE_ACTOR_CTOR);
        state.write('(');
        visitProcDef(instr.procDef, state);
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitCreateProcInstr(CreateProcInstr instr, FormatterState state) throws Exception {
        state.write($CREATE_PROC);
        state.write('(');
        visitProcDef(instr.procDef, state);
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitCreateRecInstr(CreateRecInstr instr, FormatterState state) throws Exception {
        state.write($CREATE_REC);
        state.write('(');
        accept(instr.recDef, state.inline());
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitCreateTupleInstr(CreateTupleInstr instr, FormatterState state) throws Exception {
        state.write($CREATE_TUPLE);
        state.write('(');
        accept(instr.tupleDef, state.inline());
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitDebugInstr(DebugInstr instr, FormatterState state) throws Exception {
        accept(instr.nextInstr(), state);
        return null;
    }

    @Override
    public final Void visitDisentailsInstr(DisentailsInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($NE, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitDivideInstr(DivideInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($DIV, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitEntailsInstr(EntailsInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($EQ, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public Void visitEnv(Env env, FormatterState state) throws Exception {
        String envFormatted = env.formatValue();
        if (!envFormatted.isBlank()) {
            String[] lines = envFormatted.split("\n");
            for (int i = 0; i < lines.length; i++) {
                state.write("// ");
                state.write(lines[i]);
                if (i + 1 < lines.length) {
                    state.writeNewLineAndIndent();
                }
            }
        }
        return null;
    }

    @Override
    public final Void visitFailedValue(FailedValue kernel, FormatterState state) throws Exception {
        state.write("FailedValue(error=");
        if (kernel.error() == null) {
            state.write("null");
        } else {
            accept(kernel.error(), state.inline());
        }
        state.write(')');
        return null;
    }

    @Override
    public Void visitFieldDef(FieldDef kernel, FormatterState state) throws Exception {
        accept(kernel.feature, state.inline());
        state.write(": ");
        accept(kernel.value, state.inline());
        return null;
    }

    @Override
    public Void visitFieldPtn(FieldPtn kernel, FormatterState state) throws Exception {
        accept(kernel.feature, state.inline());
        state.write(": ");
        accept(kernel.value, state.inline());
        return null;
    }

    @Override
    public final Void visitGetCellValueInstr(GetCellValueInstr instr, FormatterState state) throws Exception {
        state.write($GET);
        state.write('(');
        accept(instr.cell, state.inline());
        state.write(", ");
        accept(instr.target, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitGreaterThanOrEqualToInstr(GreaterThanOrEqualToInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($GE, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitGreaterThanInstr(GreaterThanInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($GT, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitIdent(Ident kernel, FormatterState state) throws Exception {
        if (Ident.isSimpleName(kernel.name)) {
            state.write(kernel.name);
        } else {
            state.write(Ident.quote(kernel.name));
        }
        return null;
    }

    @Override
    public final Void visitIdentDef(IdentDef kernel, FormatterState state) throws Exception {
        accept(kernel.ident, state.inline());
        if (kernel.value != null) {
            state.write(" = ");
            accept(kernel.value, state.inline());
        }
        return null;
    }

    @Override
    public Void visitIdentPtn(IdentPtn kernel, FormatterState state) throws Exception {
        if (kernel.escaped) {
            state.write('~');
        }
        accept(kernel.ident, state.inline());
        return null;
    }

    @Override
    public final Void visitIfElseInstr(IfElseInstr instr, FormatterState state) throws Exception {
        state.write("if ");
        accept(instr.x, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.consequent, nextLevelState);
        state.writeAfterNewLineAndIdent("else");
        nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.alternate, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitIfInstr(IfInstr instr, FormatterState state) throws Exception {
        state.write("if ");
        accept(instr.x, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.consequent, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public Void visitJumpCatchInstr(JumpCatchInstr kernel, FormatterState state) throws Exception {
        state.write($JUMP_CATCH);
        state.write('(');
        state.write("" + kernel.id);
        state.write(')');
        return null;
    }

    @Override
    public Void visitJumpThrowInstr(JumpThrowInstr kernel, FormatterState state) throws Exception {
        state.write($JUMP_THROW);
        state.write('(');
        state.write("" + kernel.id);
        state.write(')');
        return null;
    }

    @Override
    public final Void visitLessThanOrEqualToInstr(LessThanOrEqualToInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($LE, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitLessThanInstr(LessThanInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($LT, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitLocalInstr(LocalInstr instr, FormatterState state) throws Exception {
        state.write("local ");
        for (int i = 0; i < instr.xs.size(); i++) {
            if (i > 0) {
                state.write(',');
                state.write(FormatterState.SPACE);
            }
            IdentDef id = instr.xs.get(i);
            accept(id.ident, state.inline());
            if (id.value != null) {
                state.write(" = ");
                accept(id.value, state.inline());
            }
        }
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.body, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitModuloInstr(ModuloInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($MOD, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitMultiplyInstr(MultiplyInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($MULT, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitNegateInstr(NegateInstr instr, FormatterState state) throws Exception {
        state.write($NEGATE);
        state.write('(');
        accept(instr.a, state.inline());
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitNotInstr(NotInstr instr, FormatterState state) throws Exception {
        state.write($NOT);
        accept(instr.a, state.inline());
        state.write(", ");
        accept(instr.x, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitObj(Obj kernel, FormatterState state) throws Exception {
        state.write(kernel.formatAsKernelString());
        return null;
    }

    @Override
    public final Void visitOpaqueValue(OpaqueValue kernel, FormatterState state) throws Exception {
        state.write(kernel.getClass().getName());
        return null;
    }

    @Override
    public final Void visitProc(Proc kernel, FormatterState state) throws Exception {
        state.write(kernel.getClass().getName());
        return null;
    }

    @Override
    public final Void visitProcDef(ProcDef kernel, FormatterState state) throws Exception {
        state.write("proc (");
        for (int i = 0; i < kernel.xs.size(); i++) {
            if (i > 0) {
                state.write(',');
                state.write(FormatterState.SPACE);
            }
            accept(kernel.xs.get(i), state.inline());
        }
        state.write(") in");
        List<Ident> freeIdents = new ArrayList<>(kernel.freeIdents);
        freeIdents.sort(Comparator.comparing(a -> a.name));
        Iterator<Ident> freeIdentsIter = freeIdents.iterator();
        if (freeIdentsIter.hasNext()) {
            state.write(" // free vars: ");
        }
        while (freeIdentsIter.hasNext()) {
            Ident ident = freeIdentsIter.next();
            accept(ident, state.inline());
            if (freeIdentsIter.hasNext()) {
                state.write(", ");
            }
        }
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(kernel.instr, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public Void visitRec(Rec kernel, FormatterState state) throws Exception {
        if (kernel instanceof Tuple tuple) {
            visitTuple(tuple, null, state);
        } else {
            visitRec(kernel, null, state);
        }
        return null;
    }

    private void visitRec(Rec kernel, IdentityHashMap<Rec, Object> memos, FormatterState state) throws Exception {
        if (memos == null) {
            memos = new IdentityHashMap<>();
        }
        memos.put(kernel, Value.PRESENT);
        if (kernel.label() == Null.SINGLETON) {
            state.write('{');
        } else {
            accept(kernel.label(), state.inline());
            state.write("#{");
        }
        Collection<Var> undeterminedVars = kernel.sweepUndeterminedVars();
        if (!undeterminedVars.isEmpty()) {
            for (Var v : undeterminedVars) {
                state.write(v.formatValue());
            }
        } else {
            for (int i = 0; i < kernel.fieldCount(); i++) {
                if (i > 0) {
                    state.write(", ");
                }
                accept(kernel.featureAt(i), state.inline());
                state.write(": ");
                visitRecValue(kernel.valueAt(i).resolveValueOrVar(), memos, state);
            }
        }
        state.write('}');
    }

    @Override
    public Void visitRecDef(RecDef kernel, FormatterState state) throws Exception {
        if (kernel.label.equals(Rec.DEFAULT_LABEL)) {
            state.write('{');
        } else {
            accept(kernel.label, state.inline());
            state.write("#{");
        }
        for (int i = 0; i < kernel.fieldCount(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            FieldDef fd = kernel.fieldDefAtIndex(i);
            accept(fd, state.inline());
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitRecPtn(RecPtn kernel, FormatterState state) throws Exception {
        if (kernel.label().equals(Rec.DEFAULT_LABEL)) {
            state.write('{');
        } else {
            accept(kernel.label(), state.inline());
            state.write("#{");
        }
        for (int i = 0; i < kernel.fieldCount(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            FieldPtn fp = kernel.fields().get(i);
            accept(fp, state.inline());
            if (i + 1 == kernel.fieldCount() && kernel.partialArity()) {
                state.write(", ...");
            }
        }
        state.write('}');
        return null;
    }

    private void visitRecValue(ValueOrVar value, IdentityHashMap<Rec, Object> memos, FormatterState state) throws Exception {
        if (value instanceof Rec recValue) {
            if (memos.containsKey(recValue)) {
                state.write("<<$circular " + Kernel.toSystemString(recValue) + ">>");
            } else {
                if (recValue instanceof Tuple tupleValue) {
                    visitTuple(tupleValue, memos, state.inline());
                } else {
                    visitRec(recValue, memos, state.inline());
                }
            }
        } else {
            accept(value, state.inline());
        }
    }

    @Override
    public Void visitResolvedFieldPtn(ResolvedFieldPtn kernel, FormatterState state) throws Exception {
        accept(kernel.feature, state.inline());
        state.write(": ");
        accept(kernel.value, state.inline());
        return null;
    }

    @Override
    public final Void visitResolvedIdentPtn(ResolvedIdentPtn kernel, FormatterState state) throws Exception {
        accept(kernel.ident, state.inline());
        return null;
    }

    @Override
    public final Void visitResolvedRecPtn(ResolvedRecPtn kernel, FormatterState state) throws Exception {
        accept(kernel.label, state.inline());
        state.write("#{");
        for (int i = 0; i < kernel.fieldCount(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            ResolvedFieldPtn fp = kernel.fields.get(i);
            accept(fp, state.inline());
            if (i + 1 == kernel.fieldCount() && kernel.partialArity) {
                state.write(", ...");
            }
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitScalar(Scalar kernel, FormatterState state) throws Exception {
        state.write(kernel.formatAsKernelString());
        return null;
    }

    @Override
    public final Void visitSelectAndApplyInstr(SelectAndApplyInstr instr, FormatterState state) throws Exception {
        state.write($SELECT_APPLY);
        state.write('(');
        accept(instr.rec, state.inline());
        state.write(", [");
        for (int i = 0; i < instr.path.size(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            FeatureOrIdent f = instr.path.get(i);
            accept(f, state.inline());
        }
        state.write(']');
        if (!instr.args.isEmpty()) {
            state.write(", ");
            for (int i = 0; i < instr.args.size(); i++) {
                if (i > 0) {
                    state.write(',');
                    state.write(FormatterState.SPACE);
                }
                CompleteOrIdent y = instr.args.get(i);
                accept(y, state.inline());
            }
        }
        state.write(')');
        return null;
    }

    @Override
    public final Void visitSelectInstr(SelectInstr instr, FormatterState state) throws Exception {
        state.write($SELECT);
        state.write('(');
        accept(instr.rec, state.inline());
        state.write(", ");
        accept(instr.feature, state.inline());
        state.write(", ");
        accept(instr.target, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitSeqInstr(SeqInstr instr, FormatterState state) throws Exception {
        List<Instr> list = instr.list;
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                state.writeNewLineAndIndent();
            }
            accept(list.get(i), state);
        }
        return null;
    }

    @Override
    public final Void visitSetCellValueInstr(SetCellValueInstr instr, FormatterState state) throws Exception {
        state.write($SET);
        state.write('(');
        accept(instr.cell, state.inline());
        state.write(", ");
        accept(instr.value, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitSkipInstr(SkipInstr instr, FormatterState state) throws Exception {
        state.write("skip");
        return null;
    }

    @Override
    public Void visitStack(Stack kernel, FormatterState state) throws Exception {
        for (Stack s = kernel; s != null; s = s.next) {
            state.write(Kernel.toSystemString(s.instr));
            if (s.next != null) {
                state.writeNewLineAndIndent();
            }
        }
        return null;
    }

    @Override
    public final Void visitSubtractInstr(SubtractInstr instr, FormatterState state) throws Exception {
        formatBinaryInstr($SUB, instr.a, instr.b, instr.x, state);
        return null;
    }

    @Override
    public final Void visitThrowInstr(ThrowInstr instr, FormatterState state) throws Exception {
        state.write("throw ");
        accept(instr.error, state.inline());
        return null;
    }

    @Override
    public final Void visitTryInstr(TryInstr instr, FormatterState state) throws Exception {
        // try
        //     ....
        //     ....
        state.write("try");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        accept(instr.body, nextLevelState);
        // catch <ident>
        //     ....
        //     ....
        // end
        state.writeNewLineAndIndent();
        accept(instr.catchInstr, state);
        return null;
    }

    private void visitTuple(Tuple tuple, IdentityHashMap<Rec, Object> memos, FormatterState state) throws Exception {
        if (memos == null) {
            memos = new IdentityHashMap<>();
        }
        memos.put(tuple, Value.PRESENT);
        if (tuple.label() == Null.SINGLETON) {
            state.write('[');
        } else {
            accept(tuple.label(), state.inline());
            state.write("#[");
        }
        // Force the record to resolve available values
        tuple.checkDetermined();
        for (int i = 0; i < tuple.fieldCount(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            visitRecValue(tuple.valueAt(i).resolveValueOrVar(), memos, state);
        }
        state.write(']');
    }

    @Override
    public Void visitTupleDef(TupleDef kernel, FormatterState state) throws Exception {
        if (kernel.label.equals(Rec.DEFAULT_LABEL)) {
            state.write('[');
        } else {
            accept(kernel.label, state.inline());
            state.write("#[");
        }
        for (int i = 0; i < kernel.valueCount(); i++) {
            if (i > 0) {
                state.write(", ");
            }
            ValueDef vd = kernel.valueDefAtIndex(i);
            accept(vd.value, state.inline());
        }
        state.write(']');
        return null;
    }

    @Override
    public final Void visitValueDef(ValueDef kernel, FormatterState state) {
        throw new NeedsImpl();
    }

    @Override
    public final Void visitVar(Var var, FormatterState state) throws Exception {
        state.write(var.formatValue());
        return null;
    }

    @Override
    public final Void visitVarSet(VarSet varSet, FormatterState state) throws Exception {
        state.write(varSet.formatValue());
        return null;
    }

}
