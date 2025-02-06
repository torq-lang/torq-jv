/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.ListTools;
import org.torqlang.util.NeedsImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.torqlang.lang.TypeEnv.ADD_IDENT;
import static org.torqlang.lang.TypeEnv.SUBTRACT_IDENT;

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
 *       | \x → e                        [fn]
 *       | let x = e1 in e2              [let]
 *
 * Hindley-Milner monotypes and polytypes
 *
 *     τ = α                           [var]
 *       | C τ1 ... τn                 [app]
 *     σ = τ                           [monotype]
 *       | ∀α. σ                       [quantifier]
 *
 * Notation used to describe Hindley-Milner type proofs:
 *
 *     Γ : type environment implemented by TypeEnv
 *     ⊢ : Proves (or entails)
 *     !⊢ : Does not prove (or disentails)
 *     <- : pattern deconstruction
 *
 *
 * Hindley-Milner type proofs:
 *
 *     [con]
 *         Γ ⊢ () : ι
 *
 *     [var]
 *         Γ(x) >- τ
 *         ---------
 *         Γ ⊢ x : τ
 *
 *     [fn]
 *         Γ + x : τ1 ⊢ e : τ1
 *         -------------------
 *         Γ ⊢ λx.e : τ1 -> τ2
 *
 *     [app]
 *         Γ ⊢ e1 : τ1 -> τ2   Γ ⊢ e2 : τ1
 *         -------------------------------
 *                 Γ ⊢ e1 e2 : τ2
 *
 *     [let]
 *         Γ ⊢ e1 : τ1   Γ + x : x:ClosΓ(τ1) ⊢ e2 : τ2
 *         -------------------------------------------
 *                 Γ ⊢ let x = e1 in e2 : τ2
 *
 *     [fix]
 *         Γ + f τ ⊢ λx.e : τ
 *         ------------------
 *         Γ ⊢ fix f λx.e : τ
 *
 *     - - - - - - - additions to original Hindley-Milner type proofs - - - - - - -
 *
 *     [vardecl]
 *         x1 : τ1 ∉ Γ   x2 : τ2 ∉ Γ   ...   xn : τn ∉ Γ
 *         ---------------------------------------------
 *         Γ + x1 : τ1   Γ + x2 : τ2   ...   Γ + xn : τn
 *         ---------------------------------------------
 *            Γ ⊢ var x1 : τ1, x2 : τ2, ..., xn : τn
 *
 *     [label]
 *         Γ ⊢ e : (l : τ # {f1 : τ1, ..., fn : τn})
 *         -----------------------------------------
 *                     Γ ⊢ label(e) : τ
 *
 *     [feat]
 *         Γ ⊢ e : (l : τ # {f1 : τ1, ..., fn : τn})
 *         -----------------------------------------
 *                   Γ ⊢ feat(e, fi) : τi
 *
 *     [assign]
 *         Γ ⊢ e1 : t   Γ ⊢ e2 : t
 *         -----------------------
 *          Γ ⊢ (e1 = e2) : Void
 *
 *     [seq]
 *         Γ ⊢ e1 : Void   ...   Γ ⊢ en-1 : Void   Γ ⊢ en : τn
 *         ---------------------------------------------------
 *                    Γ ⊢ (e1; ...; en-1; en;) : τn
 *
 *     [if]
 *         Γ ⊢ b1 : Bool   Γ ⊢ e1 : τ   Γ ⊢ b2 : Bool   Γ ⊢ e2 : τ   ...   Γ ⊢ bn : Bool   Γ ⊢ en : τ   Γ ⊢ ex : τ
 *         -------------------------------------------------------------------------------------------------------
 *                        Γ ⊢ if b1 then e1 elseif b2 then e2 ... elseif bn then en else ex end : τ
 *
 *     [match]
 *         e : (l : τ # {f1 : τ1, ..., fn : τn}) ∈ Γ    r : (l : τ # {f1 : τ1, ..., fn : τn}) ∈ Γ
 *         --------------------------------------------------------------------------------------
 *                        Γ <- e of r   Γ + l : τ   Γ + f1 : τ1   ...   Γ + fn : τn
 *                        ---------------------------------------------------------
 *                               Γ ⊢ l : τ    Γ ⊢ f1 : τ1   ...   Γ ⊢ fn : τn
 *
 *     [nomatch]
 *         e : (l : τ # {f1 : τ1, ..., fn : τn}) ∈ Γ    r : (l : τ # {f1 : τ1, ..., fn : τn}) ∉ Γ
 *         --------------------------------------------------------------------------------------
 *                                          Γ <- e of r   Γ + {}
 *                        ---------------------------------------------------------
 *                             Γ !⊢ l : τ    Γ !⊢ f1 : τ1   ...   Γ !⊢ fn : τn
 *
 *     [case]
 *         Γ ⊢ e : τ   Γ <- e of r1   ...   Γ <- e of rn   Γ ⊢ e1 : τ1   ...   Γ ⊢ en : τ1   Γ ⊢ ex : τ1
 *         ---------------------------------------------------------------------------------------------
 *                          Γ ⊢ case e of r1 then e1 ... of rn then en else ex end : τ1
 *
 * Notation used to describe Hindley-Milner type inference using Algorithm M with additions:
 *
 *     M : TypEnv x Expr x Type -> Subst
 *     U : the unification algorithm
 *     Γ : type environment implemented by TypeEnv
 *     -> : function arrow
 *     <- : pattern deconstruction
 *     >- : var instantiation
 *     →α : a vector {α1, ··· , αn}
 *     {→τ/→α} : shorthand for a substitution {τi/αi | 1 ≤ i ≤ n}, where →α and →τ have the same length n
 *     LET, IN, FIX : the keywords 'let', 'in', and 'fix', respectively
 *
 * Hindley-Milner type inference using Algorithm M as it appears in
 * "Proofs about a folklore let-polymorphic type inference algorithm" with additions:
 *
 *     M = TypeEnv x Expr x Type -> Subst
 *
 *     [con]
 *         M(Γ, (), ρ) =
 *             U(ρ, ι)
 *
 *     [var]
 *         M(Γ, x, ρ) =
 *             U(ρ, {→β/→α}τ) where Γ(x) = ∀→α.τ, new →β
 *
 *     [fn]
 *         M(Γ, λx.e, ρ) =
 *             let S1 = U(ρ, β1 -> β2), new β1, β2
 *                 S2 = M(S1(Γ) + x:S1(β1), e, S1(β2))
 *             in  S2(S1)
 *
 *     [app]
 *         M(Γ, e1 e2, ρ) =
 *             let S1 = M(Γ, e1, β -> ρ), new β
 *                 S2 = M(S1(Γ), e2, S1(β))
 *             in  S2(S1)
 *
 *     [let]
 *         M(Γ, LET x = e1 IN e2, ρ) =
 *             let S1 = M(Γ, e1, β), new β
 *                 S2 = M(S1(Γ) + x:ClosS1Γ(S1(β)), e2, S1(ρ))
 *             in  S2(S1)
 *
 *     [fix]
 *         M(Γ, FIX f λx.e, ρ) =
 *             M(Γ + f:ρ, λx.e, ρ)
 *
 *     - - - - - - - additions to original Hindley-Milner type inference - - - - - - -
 *
 *     [assign]
 *         M(Γ, e1 = e2, ρ) =
 *             let S1 = M(Γ, e1, β), new β
 *                 S2 = M(S1(Γ), e2, S1(β))
 *                 S3 = unify(S2(S1(ρ)), Void)
 *             in  S3(S2(S1))
 *
 *     [seq]
 *         M(Γ, e1; e2; ...; en;, ρ) =
 *             let S1 = M(Γ, e1, Void)
 *                 S2 = M(S1(Γ), e2, Void)
 *                 ...
 *                 Sn = M(Sn-1(...S1(Γ)), en, ρ)
 *             in Sn(...(S1))
 *
 *     [if]
 *         M(Γ, if b1 then e1 elseif b2 then e2 ... elseif bn then en else ex end, ρ) =
 *             let S1 = M(Γ, b1, Bool)
 *                 S2 = M(S1(Γ), e1, ρ)
 *                 S3 = M(S2(S1(Γ)), b2, Bool)
 *                 S4 = M(S3(S2(S1(Γ))), e2, S3(S2(S1(ρ))))
 *                 ...
 *                 Sn-1 = M(Sn-2(...S1(Γ)), bn, Bool)
 *                 Sn = M(Sn-1(...S1(Γ)), en, Sn-1(...S1(ρ)))
 *                 Sn+1 = M(Sn(...S1(Γ)), ex, Sn(...S1(ρ)))
 *             in Sn+1(...(S1))
 *
 *     [case]
 *         M(Γ, case e of r1 then e1 ... of rn then en else ex end, ρ) =
 *             TODO
 *
 *     NOTE: ClosΓ(τ) = ∀→α.τ where →α = ftv(τ) \ ftv(Γ) and ClosS1Γ(τ) = ∀→α.τ where →α = ftv(τ) \ ftv(S1(Γ)).
 *           In essence, for ClosΓ(τ), add quantifiers for ftv(τ), but ensure that none of ftv(τ) are also in ftv(Γ).
 *
 * Unification algorithm
 *
 *     U = MonoType x MonoType -> Subst
 *     See TypeSubst::unify(MonoType a, MonoType b)
 *
 * For the purpose of type inference, a Stmt is an expression that evaluates to the KLVM Void type.
 *
 * Invoking algorithm M is similar to invoking a visitor method
 *
 *     M = TypeEnv x Expr x Type -> Subst = visit(Lang, (TypeEnv, Type)) -> TypeSubst,
 *     where Expr = Lang and TypeSubst = Subst
 *
 * TypeEnv and scoping
 *
 *     In the lambda calculus expression, `(\x.x)x`, `x` is both free and bound because of lexical scoping. Type
 *     checking the nested expression `\x.x` using algorithm M invokes `M(S1(Γ) + x : S1(β1), e, S1(β2))`, which
 *     brings `x` into a new scope, by creating a new type environment with `x` bound to the result of `x : S1(β1)`.
 *     Consequently, it shadows the global `x` in the outer expression `(\x.x)x`. Note that type checking
 *     `M({}, (\x.x)x, ρ)` would fail to determine a type because the initial environment does not contain an entry for
 *     `x` in the outermost scope.
 *
 * An imperative type environment with functional qualities
 *
 *     Validator implements a mutable type environment as a monotonic map with nesting. You can get existing elements
 *     by name, and you can add new elements by name if that name is not already present. You cannot remove elements.
 *     To enter a new lexical scope, create a child of an existing type environment. The new scope can shadow variables
 *     present in its ancestors. To exit a lexical scope, simply reference the parent type environment.
 *
 *     Note that the lambda expression `\x.y.(x + y)` creates two scopes because it involves two function abstractions.
 *     An equivalent function with the signature `(x, y) -> Int32` creates just one scope. Therefore, when mapping to
 *     algorithm M, we don't necessarily have to create child environments for additional arguments beyond the first
 *     one.
 */
public class Validator implements LangVisitor<TypeScope, TypeSubst> {

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

    /*
     * Create a function type representing the formal arguments and return type. The given type environment is used
     * to look up type identifiers.
     */
    private FuncType createFuncType(Lang lang, List<Pat> formalArgs, TypeAnno returnType, TypeEnv typeEnv) {
        List<MonoType> params = new ArrayList<>(formalArgs.size() + 1);
        for (Pat arg : formalArgs) {
            // TODO: Need to resolve ALL Pat types, not just IdentAsPat
            if (arg instanceof IdentAsPat identAsPat) {
                params.add(resolveTypeAnno(identAsPat, identAsPat.typeAnno, typeEnv));
            } else {
                params.add(suffixFactory.nextBetaVar());
            }
        }
        params.add(resolveTypeAnno(lang, returnType, typeEnv));
        return FuncType.create(params);
    }

    private MonoType resolveTypeAnno(Lang lang, TypeAnno typeAnno, TypeEnv typeEnv) {
        if (typeAnno != null) {
            PolyType declaredType = typeEnv.get(typeAnno.ident);
            if (declaredType == null) {
                throw new TypeNotFoundError(lang);
            }
            return declaredType.instantiate(suffixFactory);
        } else {
            return suffixFactory.nextBetaVar();
        }
    }

    private TypeSubst unify(Lang origin, MonoType expected, MonoType provided) {
        try {
            return TypeSubst.unify(expected, provided);
        } catch (TypeUnificationError error) {
            throw new TypeConflictError(origin, expected, provided);
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
    public final TypeSubst visitApplyLang(ApplyLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        return visitFuncApp(lang, scope.typeEnv(),
            new LangOrIdent(lang.proc, null),
            lang.args,
            scope.monoType()
        );
    }

    @Override
    public final TypeSubst visitAskStmt(AskStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    @Override
    public final TypeSubst visitBeginLang(BeginLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        // BeginLang will assume the type of its enclosed sequence
        // TODO:
        //      Add owner and env-type to TypeEnv, we need to distinguish between proc, begin, etc. when
        //      determining a jump-point type. For example, return statements.
        TypeEnv nestedTypeEnv = TypeEnv.create(scope.typeEnv());
        return lang.body.accept(this, new TypeScope(nestedTypeEnv, scope.monoType()));
    }

    /*
     * [con]
     *     Γ ⊢ () : ι
     *
     * [con]
     *     M(Γ, (), ρ) =
     *         U(ρ, ι)
     */
    @Override
    public final TypeSubst visitBoolAsExpr(BoolAsExpr lang, TypeScope scope) {
        lang.setTypeScope(scope);
        return unify(lang, scope.monoType(), ScalarType.BOOL);
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

    /*
     * [var]
     *     Γ(x) >- τ
     *     ---------
     *     Γ ⊢ x : τ
     *
     * [var]
     *     M(Γ, x, ρ) =
     *         U(ρ, {→β/→α}τ) where Γ(x) = ∀→α.τ, new →β
     *
     * [app]
     *     Γ ⊢ e1 : τ1 -> τ2   Γ ⊢ e2 : τ1
     *     -------------------------------
     *             Γ ⊢ e1 e2 : τ2
     *
     * [app]
     *     M(Γ, e1 e2, ρ) =
     *         let S1 = M(Γ, e1, β -> ρ), new β
     *             S2 = M(S1(Γ), e2, S1(β))
     *         in  S2(S1)
     *
     * [app]
     *     An iterative interpretation of algorithm M. Type environment Γ is mutable in just two ways: its set of keys
     *     (domain) can grow by appending elements, and mapped values can change when we apply substitutions learned
     *     through type inference.
     *         Set S1 to substitutions that constrain ρ to the return type of the func body type:
     *             Infer type of e1 (the function expression) constrained to `(β1, ..., βn) -> ρ`
     *         Infer type of each function parameter e2i, where i is 1 to n in β1, ..., βn
     *             Apply S1 to β1, ..., βn
     *             S1 = S1(M(S1(Γ), e2i, βi))
     *         Return the total substitutions learned in this step of type inference
     *             return S1
     */
    private TypeSubst visitFuncApp(Lang origin, TypeEnv typeEnv, LangOrIdent func, List<StmtOrExpr> args, MonoType rho)
        throws Exception
    {
        // Set S1 to substitutions that constrain ρ as the return type and the func body type:
        List<MonoType> betas = new ArrayList<>(args.size() + 1);
        for (int i = 0; i < args.size(); i++) {
            betas.add(suffixFactory.nextBetaVar());
        }
        betas.add(rho);
        FuncType funcType = FuncType.create(betas);
        TypeSubst s1;
        if (func.ident != null) {
            MonoType tau = typeEnv.get(func.ident).instantiate(suffixFactory);
            s1 = unify(origin, funcType, tau);
        } else {
            s1 = func.lang.accept(this, new TypeScope(typeEnv, funcType));
        }
        // Infer type of each function parameter e2i, where i is 1 to n in β1, ..., βn
        for (int i=0; i < args.size(); i++) {
            StmtOrExpr argExpr = args.get(i);
            betas = betas.stream().map(s1::apply).toList();
            TypeSubst argSubst = argExpr.accept(this, new TypeScope(s1.apply(typeEnv), betas.get(i)));
            s1 = argSubst.apply(s1);
        }
        // Return the total substitutions learned in this step of type inference
        return s1;
    }

    /*
     * [fn]
     *     Γ + x : τ1 ⊢ e : τ1
     *     -------------------
     *     Γ ⊢ λx.e : τ1 -> τ2
     *
     * [fn]
     *     M(Γ, λx.e, ρ) =
     *         let S1 = U(ρ, β1 -> β2), new β1, β2
     *             S2 = M(S1(Γ) + x : S1(β1), e, S1(β2))
     *         in  S2(S1)
     *
     * [fn]
     *     An iterative interpretation of algorithm M. Type environment Γ is mutable in just two ways: its set of keys
     *     (domain) can grow by appending elements, and mapped values can change when we apply substitutions learned
     *     through type inference.
     *         Set S1 to substitutions that constrain ρ to a function type
     *             Unify ρ and `(β1, ..., βn) -> βn+1`
     *         Append a type assignment for each param xi, where i is 1 to n in β1, ..., βn
     *             S1 = S1(M(S1(Γ), xi, S1(βi)))
     *         Set S2 to substitutions that constrain the return type to the body type
     *             S2 = M(S1(Γ), e, S1(βn+1))
     *         Return the total substitutions learned in this step of type inference
     *             return S2(S1)  // Substitutions do not commute, we must apply S2 to S1
     */
    public final TypeSubst visitFuncScope(Lang origin, TypeEnv typeEnv, List<Pat> params, SeqLang body, MonoType rho)
        throws Exception
    {
        // Set S1 to substitutions that constrain ρ to a function type
        List<MonoType> betas = new ArrayList<>();
        for (int i = 0; i < params.size() + 1; i++) {
            betas.add(suffixFactory.nextBetaVar());
        }
        TypeSubst s1 = unify(origin, rho, FuncType.create(betas));
        // Append a type assignment for each param xi, where i is 1 to n in β1, ..., βn
        for (int i = 0; i < params.size(); i++) {
            Pat param = params.get(i);
            MonoType beta = betas.get(i);
            // Note that visiting Torq patterns adds them to the type environment
            TypeSubst paramSubst = param.accept(this, new TypeScope(s1.apply(typeEnv), s1.apply(beta)));
            s1 = paramSubst.apply(s1);
        }
        MonoType beta = ListTools.last(betas);
        // Set S2 to substitutions that constrain the return type to the body type
        TypeSubst s2 = body.accept(this, new TypeScope(s1.apply(typeEnv), s1.apply(beta)));
        // Return the total substitutions learned in this step of type inference
        return s2.apply(s1);
    }

    /*
     * [fix]
     *     Γ + f τ ⊢ λx.e : τ
     *     ------------------
     *     Γ ⊢ fix f λx.e : τ
     *
     * [fix]
     *     M(Γ, FIX f λx.e, ρ) =
     *         M(Γ + f:ρ, λx.e, ρ)
     *
     * Add a function type to the given type environment:
     *     Unify the function statement type with Void
     *     Create a function type to be assigned to the function name
     *     Validate a type is not already assigned to the function name
     *     Add the "name : function-type" assignment to the type environment
     *     Create a child environment as a nested lexical scope
     *     Infer the type of the function body constrained to the function type and child environment
     */
    @Override
    public final TypeSubst visitFuncStmt(FuncStmt lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeEnv thisTypeEnv = scope.typeEnv();
        // Unify the function statement type with Void
        TypeSubst s1 = unify(lang, scope.monoType(), ScalarType.VOID);
        s1.apply(thisTypeEnv);
        // Create a function type to be assigned to the function name
        FuncType funcType = createFuncType(lang, lang.formalArgs, lang.returnType, thisTypeEnv);
        // Validate a type is not already assigned to the function name
        PolyType alreadyDefinedType = thisTypeEnv.shallowGet(lang.name());
        if (alreadyDefinedType != null) {
            throw new AlreadyDefinedInScopeError(lang);
        }
        // Add the "name : function-type" assignment to the type environment
        thisTypeEnv.put(lang.name(), funcType);
        // Create a child environment as a nested lexical scope
        TypeEnv nestedTypeEnv = TypeEnv.create(thisTypeEnv);
        // Infer the type of the function body constrained to the function type and child environment
        TypeSubst s2 = visitFuncScope(lang, nestedTypeEnv, lang.formalArgs, lang.body, funcType);
        return s2.apply(s1);
    }

    @Override
    public final TypeSubst visitGroupExpr(GroupExpr lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    private TypeSubst visitIdent(Lang lang, Ident ident, TypeScope scope) {
        PolyType polyType = scope.typeEnv().get(ident);
        if (polyType == null) {
            throw new NotDefinedError(lang);
        }
        MonoType freshType = polyType.instantiate(suffixFactory);
        return unify(lang, scope.monoType(), freshType);
    }

    @Override
    public final TypeSubst visitIdentAsExpr(IdentAsExpr lang, TypeScope scope) {
        lang.setTypeScope(scope);
        return visitIdent(lang, lang.ident, scope);
    }

    /*
     * Visiting an identifier as a pattern:
     *     1) Cannot be an illegal name (can't shadow a builtin identifier)
     *     2) Cannot already be defined in current scope
     *     3) Declared type is unified with rho
     *     4) Substitutions from unification are applied to given TypeEnv
     *     5) Substitutions are applied to declared type
     *     6) An entry is added to the given TypeEnv
     * In summary:
     *     1) Add an entry to the given TypeEnv
     *     2) Apply substitution effects to the TypeEnv
     *     3) Return combined substitutions to the caller
     */
    @Override
    public final TypeSubst visitIdentAsPat(IdentAsPat lang, TypeScope scope) {
        // TODO: Destructure identifier into current typeEnv, bringing a new identifier into scope
        //       This implies that the caller created a nestedTypeEnv appropriately.
        //       See visitRecPat()
        lang.setTypeScope(scope);
        if (ILLEGAL_IDENTS.contains(lang.ident)) {
            throw new IllegalIdentError(lang);
        }
        if (lang.escaped) {
            return visitIdent(lang, lang.ident, scope);
        }
        TypeEnv thisTypeEnv = scope.typeEnv();
        PolyType alreadyDefinedType = thisTypeEnv.shallowGet(lang.ident);
        if (alreadyDefinedType != null) {
            throw new AlreadyDefinedInScopeError(lang);
        }
        // Get declared type from annotation or create a fresh beta
        MonoType patType = resolveTypeAnno(lang, lang.typeAnno, thisTypeEnv);
        TypeSubst result = unify(lang, scope.monoType(), patType);
        result.apply(thisTypeEnv);
        // Add declaration to the type environment
        thisTypeEnv.put(lang.ident, result.apply(patType));
        return result;
    }

    @Override
    public final TypeSubst visitIdentVarDecl(IdentVarDecl lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        VarType varType = suffixFactory.nextBetaVar();
        return lang.identAsPat.accept(this, new TypeScope(scope.typeEnv(), varType));
    }

    @Override
    public final TypeSubst visitIfClause(IfClause lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    /*
     * [if]
     *     Γ ⊢ b1 : Bool   Γ ⊢ e1 : τ   Γ ⊢ b2 : Bool   Γ ⊢ e2 : τ ...   Γ ⊢ bn : Bool   Γ ⊢ en : τ   Γ ⊢ ex : τ
     *     -----------------------------------------------------------------------------------------------------
     *                   Γ ⊢ if b1 then e1 elseif b2 then e2 ... elseif bn then en else ex end : τ
     *
     * [if]
     *     M(Γ, if b1 then e1 elseif b2 then e2 ... elseif bn then en else ex end, ρ) =
     *         let S1 = M(Γ, b1, Bool)
     *             S2 = M(S1(Γ), e1, ρ)
     *             S3 = M(S2(S1(Γ)), b2, Bool)
     *             S4 = M(S3(S2(S1(Γ))), e2, S3(S2(S1(ρ))))
     *             ...
     *             Sn-1 = M(Sn-2(...S1(Γ)), bn, Bool)
     *             Sn = M(Sn-1(...S1(Γ)), en, Sn-1(...S1(ρ)))
     *             Sn+1 = M(Sn(...S1(Γ)), ex, Sn(...S1(ρ)))
     *         in Sn+1(...(S1))
     */
    @Override
    public final TypeSubst visitIfLang(IfLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeSubst s1 = lang.ifClause.condition.accept(this, new TypeScope(scope.typeEnv(), ScalarType.BOOL));
        TypeEnv nestedTypeEnv = TypeEnv.create(scope.typeEnv());
        TypeSubst s2 = lang.ifClause.body.accept(this, new TypeScope(s1.apply(nestedTypeEnv), scope.monoType()));
        TypeSubst sn = s2.apply(s1);
        for (IfClause ifClause : lang.altIfClauses) {
            s1 = ifClause.condition.accept(this, new TypeScope(sn.apply(scope.typeEnv()), ScalarType.BOOL));
            sn = s1.apply(sn);
            nestedTypeEnv = TypeEnv.create(scope.typeEnv());
            s2 = ifClause.body.accept(this, new TypeScope(sn.apply(nestedTypeEnv), sn.apply(scope.monoType())));
            sn = s2.apply(sn);
        }
        nestedTypeEnv = TypeEnv.create(scope.typeEnv());
        TypeSubst s = lang.elseSeq.accept(this, new TypeScope(sn.apply(nestedTypeEnv), sn.apply(scope.monoType())));
        return s.apply(sn);
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

        // TODO: type-check declarations that include a type annotation AND an init value, such as:
        //       var x::Int32 = 5
        //       var x::Int32 = some_function()

        TypeSubst unifySubst = unify(lang, result.apply(varBeta), result.apply(valueBeta));
        return TypeSubst.combine(unifySubst, result);
    }

    /*
     * [con]
     *     Γ ⊢ () : ι
     *
     * [con]
     *     M(Γ, (), ρ) =
     *         U(ρ, ι)
     */
    private TypeSubst visitInt(Lang lang, Int64 int64, TypeScope scope) {
        lang.setTypeScope(scope);
        TypeSubst result;
        if (int64 instanceof Int32) {
            result = unify(lang, scope.monoType(), ScalarType.INT32);
        } else {
            result = unify(lang, scope.monoType(), ScalarType.INT64);
        }
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
        // TODO: Destructure record into current typeEnv, bringing new identifiers into scope
        //       This implies that the caller created a nestedTypeEnv appropriately.
        //       See visitIdentAsPat()
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
     * [seq]
     *     Γ ⊢ e1 : Void   ...   Γ ⊢ en-1 : Void   Γ ⊢ en : τn
     *     ---------------------------------------------------
     *                Γ ⊢ (e1; ...; en-1; en;) : τn
     *
     * [seq]
     *     M(Γ, e1; e2; ...; en;, ρ) =
     *         let S1 = M(Γ, e1, Void)
     *             S2 = M(S1(Γ), e2, Void)
     *             ...
     *             Sn = M(Sn-1(...S1(Γ)), en, ρ)
     *         in Sn(...(S1))
     */
    @Override
    public final TypeSubst visitSeqLang(SeqLang lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeSubst sn = TypeSubst.empty();
        for (int i = 0; i < lang.list.size() - 1; i++) {
            TypeSubst s = lang.list.get(i).accept(this, new TypeScope(sn.apply(scope.typeEnv()), ScalarType.VOID));
            sn = s.apply(sn);
        }
        TypeSubst s = ListTools.last(lang.list).accept(this, new TypeScope(sn.apply(scope.typeEnv()), scope.monoType()));
        return s.apply(sn);
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
     * [app]
     *     M(Γ, e1 e2, ρ) =
     *         let S1 = M(Γ, e1, β -> ρ), new β
     *             S2 = M(S1(Γ), e2, S1(β))
     *         in  S2(S1)
     */
    @Override
    public final TypeSubst visitSumExpr(SumExpr lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        Ident sumIdent = lang.oper == SumOper.ADD ? ADD_IDENT : SUBTRACT_IDENT;
        return visitFuncApp(lang, scope.typeEnv(),
            new LangOrIdent(null, sumIdent),
            List.of(lang.arg1, lang.arg2), scope.monoType()
        );
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

    /*
     * [assign]
     *     Γ ⊢ e1 : t   Γ ⊢ e2 : t
     *     -----------------------
     *        (e1 = e2) : Void
     *
     * [assign]
     *     M(Γ, e1 = e2, ρ) =
     *         let S1 = M(Γ, e1, β), new β
     *             S2 = M(S1(Γ), e2, S1(β))
     *             S3 = unify(S2(S1(ρ)), Void)
     *         in  S3(S2(S1))
     */
    @Override
    public final TypeSubst visitUnifyStmt(UnifyStmt lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        VarType beta = suffixFactory.nextBetaVar();
        TypeSubst s1 = lang.leftSide.accept(this, new TypeScope(scope.typeEnv(), beta));
        TypeSubst s2 = lang.rightSide.accept(this, new TypeScope(s1.apply(scope.typeEnv()), s1.apply(beta)));
        TypeSubst sn = s2.apply(s1);
        TypeSubst s3 = unify(lang, sn.apply(scope.monoType()), ScalarType.VOID);
        return s3.apply(sn);
    }

    /*
     * [vardecl]
     *     x1 : τ1 ∉ Γ   x2 : τ2 ∉ Γ   ...   xn : τn ∉ Γ
     *     ---------------------------------------------
     *     Γ + x1 : τ1   Γ + x2 : τ2   ...   Γ + xn : τn
     *     ---------------------------------------------
     *        Γ ⊢ var x1 : τ1, x2 : τ2, ..., xn : τn
     */
    @Override
    public final TypeSubst visitVarStmt(VarStmt lang, TypeScope scope) throws Exception {
        lang.setTypeScope(scope);
        TypeSubst sn = TypeSubst.empty();
        for (VarDecl vd : lang.varDecls) {
            sn = vd.accept(this, new TypeScope(sn.apply(scope.typeEnv()), ScalarType.VOID));
        }
        TypeSubst s = unify(lang, scope.monoType(), ScalarType.VOID);
        sn = s.apply(sn);
        sn.apply(scope.typeEnv());
        return sn;
    }

    @Override
    public final TypeSubst visitWhileStmt(WhileStmt lang, TypeScope scope) {
        throw new NeedsImpl();
    }

    record LangOrIdent(Lang lang, Ident ident) {
    }

}
