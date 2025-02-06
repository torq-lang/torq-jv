/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.NeedsImpl;

/*
 * Validator is a top-down type inference algorithm derived from Algorithm M from the paper:
 *     Lee, Oukseh, and Kwangkeun Yi. "Proofs about a folklore let-polymorphic type inference algorithm."
 *     ACM Transactions on Programming Languages and Systems (TOPLAS) 20.4 (1998): 707-723.
 *
 * Additionally, we use information and insights provided by Adam Jones in his YouTube video series:
 * "Type systems: Lamda calculus to Hindley-Milner" at
 * https://www.youtube.com/playlist?list=PLoyEIY-nZq_uipRkxG79uzAgfqDuHzot-
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
 *     M: TypEnv × Expr × Type → Subst
 *     U: the unification algorithm implemented by TypeSubst.unify()
 *     Γ: is the type context implemented by TypeCntxt
 *     ->: is the function arrow
 *     >-: is the var instantiation symbol
 *     →α: is a vector {α1, ··· , αn}
 *     {→τ/→α}: is a shorthand for a substitution {τi/αi | 1 ≤ i ≤ n}, where →α and →τ have the same length n
 *
 * For the purpose of type inference, a Stmt is an expression that evaluates to type Void
 */
public class Validator implements LangVisitor<TypeScope, TypeSubst> {
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

    /*
     * TODO: Ensure that nested lexical scopes get a nested TypeScope
     */
    @Override
    public final TypeSubst visitBeginLang(BeginLang lang, TypeScope scope) throws Exception {
        // TODO: Create a nested scope
        return lang.body.accept(this, scope);
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

    /*
     * Hindley-Milner type rules for [var]
     *
     *     x : σ ∈ Γ
     *     --------- [var]
     *     Γ ⊢ x : σ
     *
     *     Γ ⊢ e : σa    σa ⊑ σb
     *     --------------------- [inst]
     *            Γ ⊢ e : σb
     *
     *     ABOVE IS THE SAME AS BELOW
     *
     *     Γ(x) >- τ
     *     --------- [VAR]
     *     Γ ⊢ x : τ
     *
     * Algorithm M = TypeCntxt x Expr x MonoType -> Subst
     *
     *     M(Γ, x, ρ) = U(ρ, {→β/→α}τ) where Γ(x) = ∀→α.τ, new →β
     */
    @Override
    public final TypeSubst visitIdentAsExpr(IdentAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIdentAsPat(IdentAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
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
    public final TypeSubst visitInitVarDecl(InitVarDecl lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIntAsExpr(IntAsExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitIntAsPat(IntAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
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

    /*
     * Hindley-Milner type rules for [seq]
     *
     *     Where "e1", "e2", ..., "en" are individual expressions:
     *
     *     Γ ⊢ e1 : τ1    Γ ⊢ e2 : τ2    ...    Γ ⊢ en : τn
     *     ------------------------------------------------ [seq]
     *                Γ ⊢ (e1; e2; ...; en) : τn
     *
     * Algorithm M = TypeCntxt x Expr x MonoType -> Subst
     *
     *     M(Γ, (e1; e2; ...; en), ρ) =
     *         let
     *             S1 = M(Γ, e1, β1), new β1
     *             S2 = M(S1Γ, e1, S1β2), new β2
     *             ...
     *             Sn = M(Sn-1Γ, e1, Sn-1βn), new βn
     *         in
     *             Sn-1...S2S1
     */
    @Override
    public final TypeSubst visitSeqLang(SeqLang lang, TypeScope scope) {
        throw new NeedsImpl();
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
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitStrAsPat(StrAsPat lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitSumExpr(SumExpr lang, TypeScope scope) {
        throw new NeedsImpl();
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
    public final TypeSubst visitVarStmt(VarStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitWhileStmt(WhileStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }
}
