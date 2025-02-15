/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Int64;
import org.torqlang.util.ListTools;
import org.torqlang.util.NeedsImpl;

import java.util.List;
import java.util.Set;

/*
 * Validator is a top-down type inference algorithm derived from Algorithm M from the paper:
 *     Lee, Oukseh, and Kwangkeun Yi. "Proofs about a folklore let-polymorphic type inference algorithm."
 *     ACM Transactions on Programming Languages and Systems (TOPLAS) 20.4 (1998): 707-723.
 *
 * Additionally, we use information and insights provided by Adam Jones in his YouTube video series:
 *     "Type systems: Lamda calculus to Hindley-Milner" at
 *     https://www.youtube.com/playlist?list=PLoyEIY-nZq_uipRkxG79uzAgfqDuHzot-
 *
 * Lambda Calculus
 *
 *     e =
 *       x                               [var]
 *       | e1 e2                         [app]
 *       | \x → e                        [abs]
 *       | let x = e1 in e2              [let]
 *
 * Hindley-Milner monotypes and polytypes
 *
 *     τ = α                           [var]
 *       | C τ1 ... τn                 [app]
 *     σ = τ                           [monotype]
 *       | ∀α. σ                       [quantifier]
 *
 * Notation used in this program
 *
 *     M: TypEnv x Expr x Type -> Subst
 *     U: the unification algorithm
 *     Γ: is the type environment implemented by TypeEnv
 *     ->: is the function arrow
 *     >-: is the var instantiation symbol
 *     →α: is a vector {α1, ··· , αn}
 *     {→τ/→α}: is shorthand for a substitution {τi/αi | 1 ≤ i ≤ n}, where →α and →τ have the same length n
 *
 * Unification algorithm
 *
 *     U = τ1 x τ2 -> S where τn is monotype and S is a substitution
 *     S(τ1) = S(τ2)
 *
 * For the purpose of type inference, a Stmt is an expression that evaluates to the KLVM Void type.
 */
public class Validator implements LangVisitor<TypeScope, TypeSubst> {

    private static final Ident ADD_IDENT = Ident.create("+");
    private static final Ident SUB_IDENT = Ident.create("-");

    /*
        From the book:

        ## Scalar, composite, and method types

        - Value
            - Comp
                - Obj
                - Rec
                    - Tuple
                    - Array
            - Lit
                - Bool
                - Eof
                - Null
                - Str
                - Token
            - Num
                - Dec128
                - Flt64
                    - Flt32
                - Int64
                    - Int32
                        - Char
            - Meth
                - Func
                - Proc

        ## Actor, label (literal) and feature types

        - Value
            - Comp
                - Obj
                    - ActorCfg
            - Feat
                - Int32
                - Lit
            - Meth
                - Func
                    - ActorCfgtr
     */
    private static final Set<Ident> ILLEGAL_IDENTS = Set.of(
        Ident.create("ActorCfg"),
        Ident.create("ActorCfgtr"),
        Ident.create("Array"),
        Ident.create("Bool"),
        Ident.create("Char"),
        Ident.create("Comp"),
        Ident.create("Dec128"),
        Ident.create("Feat"),
        Ident.create("Func"),
        Ident.create("Flt32"),
        Ident.create("Flt64"),
        Ident.create("Int32"),
        Ident.create("Int64"),
        Ident.create("Lit"),
        Ident.create("Meth"),
        Ident.create("Null"),
        Ident.create("Obj"),
        Ident.create("Proc"),
        Ident.create("Rec"),
        Ident.create("Tuple"),
        Ident.create("Value"),
        Ident.create("Void")
    );

    private final SuffixFactory suffixFactory;

    public Validator() {
        this(new SuffixFactory());
    }

    public Validator(SuffixFactory suffixFactory) {
        this.suffixFactory = suffixFactory;
    }

    private TypeSubst unify(Lang lang, MonoType expected, MonoType provided) {
        try {
            return TypeSubst.unify(expected, provided);
        } catch (TypeUnificationError error) {
            throw new TypeConflictError(lang, expected, provided);
        }
    }

    @Override
    public final TypeSubst visitActExpr(ActExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitActorExpr(ActorExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitActorStmt(ActorStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitAndExpr(AndExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitApplyLang(ApplyLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitAskStmt(AskStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitBeginLang(BeginLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        // BeginLang will assume the type of its enclosed sequence
        TypeScope nestedScope = new TypeScope(TypeEnv.create(scope.typeEnv()), scope.monoType());
        return lang.body.accept(this, nestedScope);
    }

    @Override
    public final TypeSubst visitBoolAsExpr(BoolAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitBoolAsPat(BoolAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitBreakStmt(BreakStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitCaseClause(CaseClause lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitCaseLang(CaseLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitCatchClause(CatchClause lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitCharAsExpr(CharAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitContinueStmt(ContinueStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitDec128AsExpr(Dec128AsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitDotSelectExpr(DotSelectExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitEofAsExpr(EofAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitEofAsPat(EofAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitFieldExpr(FieldExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitFieldPat(FieldPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitFltAsExpr(FltAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitForStmt(ForStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitFuncExpr(FuncExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitFuncStmt(FuncStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitGroupExpr(GroupExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIdentAsExpr(IdentAsExpr lang, TypeScope scope) {
        lang.setTypeScope(scope);
        PolyType polyType = scope.typeEnv().get(lang.ident);
        if (polyType == null) {
            throw new NotDefinedError(lang);
        }
        MonoType freshType = polyType.instantiate(suffixFactory);
        return unify(lang, scope.monoType(), freshType);
    }

    @Override
    public final TypeSubst visitIdentAsPat(IdentAsPat lang, TypeScope scope) {
        lang.setTypeScope(scope);
        if (lang.escaped) {
            throw new NeedsImpl();
        }
        if (ILLEGAL_IDENTS.contains(lang.ident)) {
            throw new IllegalIdentError(lang);
        }
        TypeEnv thisTypeEnv = scope.typeEnv();
        PolyType alreadyDefinedType = thisTypeEnv.shallowGet(lang.ident);
        if (alreadyDefinedType != null) {
            throw new AlreadyDefinedInScopeError(lang);
        }
        // Get declared type from annotation or create a fresh beta
        MonoType patType;
        if (lang.typeAnno != null) {
            PolyType declaredType = thisTypeEnv.get(lang.typeAnno.ident);
            if (declaredType == null) {
                throw new TypeNotFoundError(lang);
            }
            patType = declaredType.instantiate(suffixFactory);
        } else {
            patType = suffixFactory.nextBetaVar();
        }
        TypeSubst result = unify(lang, scope.monoType(), patType);
        result.apply(thisTypeEnv);
        // Add declaration to the type environment
        thisTypeEnv.put(lang.ident, result.apply(patType));
        return result;
    }

    @Override
    public final TypeSubst visitIdentVarDecl(IdentVarDecl lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIfClause(IfClause lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIfLang(IfLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitImportStmt(ImportStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIndexSelectExpr(IndexSelectExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitInitVarDecl(InitVarDecl lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();

        VarType varBeta = suffixFactory.nextBetaVar();
        TypeSubst result = lang.varPat.accept(this, new TypeScope(scope.typeEnv(), varBeta));

        VarType valueBeta = suffixFactory.nextBetaVar();
        TypeSubst valueSubst = lang.valueExpr.accept(this, new TypeScope(result.apply(thisTypeEnv), valueBeta));
        result = TypeSubst.combine(valueSubst, result);

        // TODO: validate declarations that include a type annotation AND an init value, such as:
        //       var x::Int32 = 5
        //       var x::Int32 = some_function()

        TypeSubst unifySubst = unify(lang, result.apply(varBeta), result.apply(valueBeta));
        return TypeSubst.combine(unifySubst, result);
    }

    private TypeSubst visitInt(Lang lang, Int64 int64, TypeScope scope) {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();
        TypeSubst result;
        if (int64 instanceof Int32) {
            result = unify(lang, scope.monoType(), ScalarType.INT32);
        } else {
            result = unify(lang, scope.monoType(), ScalarType.INT64);
        }
        result.apply(thisTypeEnv);
        return result;
    }

    @Override
    public final TypeSubst visitIntAsExpr(IntAsExpr lang, TypeScope scope) {
        return visitInt(lang, lang.int64(), scope);
    }

    @Override
    public final TypeSubst visitIntAsPat(IntAsPat lang, TypeScope scope) {
        return visitInt(lang, lang.int64(), scope);
    }

    @Override
    public final TypeSubst visitLocalLang(LocalLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitNullAsExpr(NullAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitNullAsPat(NullAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitOrExpr(OrExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitProcExpr(ProcExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitProcStmt(ProcStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitProductExpr(ProductExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitRecExpr(RecExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitRecPat(RecPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitRelationalExpr(RelationalExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitRespondStmt(RespondStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitReturnStmt(ReturnStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitSelectAndApplyLang(SelectAndApplyLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitSeqLang(SeqLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();
        TypeSubst result = TypeSubst.empty();
        for (int i=0; i < lang.list.size() - 1; i++) {
            result.apply(thisTypeEnv);
            result = lang.list.get(i).accept(this, new TypeScope(thisTypeEnv, ScalarType.VOID));
        }
        result.apply(thisTypeEnv);
        VarType nextBeta = suffixFactory.nextBetaVar();
        StmtOrExpr last = ListTools.last(lang.list);
        result = last.accept(this, new TypeScope(thisTypeEnv, nextBeta));
        TypeSubst unifySubst = unify(last, scope.monoType(), result.apply(nextBeta));
        return TypeSubst.combine(unifySubst, result);
    }

    @Override
    public final TypeSubst visitSetCellValueStmt(SetCellValueStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitSkipStmt(SkipStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitSpawnExpr(SpawnExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitStrAsExpr(StrAsExpr lang, TypeScope scope) {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();
        TypeSubst result = unify(lang, scope.monoType(), ScalarType.STR);
        result.apply(thisTypeEnv);
        return result;
    }

    @Override
    public final TypeSubst visitStrAsPat(StrAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    /*
     * Type inference for function application:
     *
     * M = TypeEnv x Expr x Type -> Subst
     *
     *     M(Γ, x, ρ) =
     *         U(ρ, {→β/→α}τ) where Γ(x) = ∀→α.τ, new →β
     *
     *     M(Γ, e1 e2, ρ) =
     *         S1 = M(Γ, e1, β -> ρ), new β
     *         S2 = M(S1(Γ), e2, S1(β))
     *         return S2(S1)
     */
    @Override
    public final TypeSubst visitSumExpr(SumExpr lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();

        Ident funcIdent;
        if (lang.oper.equals(SumOper.ADD)) {
            funcIdent = ADD_IDENT;
        } else {
            funcIdent = SUB_IDENT;
        }
        FuncType e1 = (FuncType) scope.typeEnv().get(funcIdent).instantiate(suffixFactory);
        MonoType param1Beta = suffixFactory.nextBetaVar();
        MonoType param2Beta = suffixFactory.nextBetaVar();
        FuncType rho = FuncType.create(List.of(param1Beta, param2Beta, scope.monoType()));
        TypeSubst result = unify(lang, rho, e1);
        result.apply(thisTypeEnv);

        VarType arg1Beta = suffixFactory.nextBetaVar();
        result = TypeSubst.combine(lang.arg1.accept(this, new TypeScope(thisTypeEnv, arg1Beta)), result);
        result.apply(thisTypeEnv);

        VarType arg2Beta = suffixFactory.nextBetaVar();
        result = TypeSubst.combine(lang.arg2.accept(this, new TypeScope(thisTypeEnv, arg2Beta)), result);
        result.apply(thisTypeEnv);

        result = TypeSubst.combine(unify(lang.arg1, result.apply(param1Beta), result.apply(arg1Beta)), result);
        result = TypeSubst.combine(unify(lang.arg2, result.apply(param2Beta), result.apply(arg2Beta)), result);

        return result;
    }

    @Override
    public final TypeSubst visitTellStmt(TellStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitThrowLang(ThrowLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitTryLang(TryLang lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitTupleExpr(TupleExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitTuplePat(TuplePat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitTypeAnno(TypeAnno lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitUnaryExpr(UnaryExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitUnifyStmt(UnifyStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitVarStmt(VarStmt lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();
        TypeSubst result = TypeSubst.empty();
        for (VarDecl vd : lang.varDecls) {
            result.apply(thisTypeEnv);
            result = vd.accept(this, new TypeScope(thisTypeEnv, ScalarType.VOID));
        }
        TypeSubst unifySubst = unify(lang, scope.monoType(), result.apply(ScalarType.VOID));
        return TypeSubst.combine(unifySubst, result);
    }

    @Override
    public final TypeSubst visitWhileStmt(WhileStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }
}
