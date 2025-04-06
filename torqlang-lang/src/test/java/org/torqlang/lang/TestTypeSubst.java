/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeSubst {

    @Test
    public void test01() {

        // s = unify(α, β)
        // s = {α |-> β}

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        TypeSubst s = TypeSubst.unify(alphaVar, betaVar);
        assertEquals(1, s.size());
        assertEquals(betaVar, s.get(alphaVar));
    }

    @Test
    public void test02() {

        // s = unify(α, Bool)
        // s = {α |-> Bool}

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        TypeSubst s = TypeSubst.unify(alphaVar, ScalarInfr.BOOL);
        assertEquals(1, s.size());
        assertEquals(ScalarInfr.BOOL, s.get(alphaVar));
    }

    @Test
    public void test03() {

        // s = unify(Bool, α)
        // s = {α |-> Bool}

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        TypeSubst s = TypeSubst.unify(ScalarInfr.BOOL, alphaVar);
        assertEquals(1, s.size());
        assertEquals(ScalarInfr.BOOL, s.get(alphaVar));
    }

    @Test
    public void test04() {

        // a = (Int32) -> α
        // b = (β) -> Bool
        // s = unify(a, b)
        // s(a) == s(b) == (Int32) -> Bool

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);

        TypeSubst s = TypeSubst.unify(alphaVar, betaVar);
        assertEquals(betaVar, s.get(alphaVar));
        assertNull(s.get(betaVar));
        assertEquals(alphaVar.subst(s), betaVar.subst(s));

        FuncInfr a = FuncInfr.create(List.of(ScalarInfr.INT32, alphaVar));
        FuncInfr b = FuncInfr.create(List.of(betaVar, ScalarInfr.BOOL));
        s = TypeSubst.unify(a, b);
        assertEquals(ScalarInfr.BOOL, s.get(alphaVar));
        assertEquals(ScalarInfr.INT32, s.get(betaVar));
        assertEquals(a.subst(s), b.subst(s));
    }

    @Test
    public void test05() {

        // --------------------------
        //  v    S2(v)    S1(S2(v))
        // --------------------------
        //  α      β          δ
        //  β      β          δ
        // --------------------------
        //
        // s1 = {α ↦ γ, β ↦ δ}
        // s2 = {α ↦ β}
        // s1(s2) = combine(s1, s2) = {α ↦ δ, δ ↦ β}
        //

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        VarInfr gammaVar = VarInfr.create(PolyInfr.LOWER_GAMMA);
        VarInfr deltaVar = VarInfr.create(PolyInfr.LOWER_DELTA);

        TypeSubst s1 = TypeSubst.create(Map.of(
            alphaVar, gammaVar,
            betaVar, deltaVar
        ));

        TypeSubst s2 = TypeSubst.create(Map.of(alphaVar, betaVar));

        TypeSubst s3 = TypeSubst.combine(s1, s2);
        assertEquals(deltaVar, s3.get(alphaVar));
        assertEquals(deltaVar, s3.get(betaVar));
    }

    @Test
    public void test06() {

        // --------------------------
        //  v    S2(v)    S1(S2(v))
        // --------------------------
        //  α      β          δ
        //  β      α          γ
        // --------------------------
        //
        // s1 = {α ↦ γ, β ↦ δ}
        // s2 = {α ↦ β, β ↦ α}
        // s1(s2) = combine(s1, s2) = {α ↦ δ, β ↦ γ}
        //

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        VarInfr gammaVar = VarInfr.create(PolyInfr.LOWER_GAMMA);
        VarInfr deltaVar = VarInfr.create(PolyInfr.LOWER_DELTA);

        TypeSubst s1 = TypeSubst.create(Map.of(
            alphaVar, gammaVar,
            betaVar, deltaVar
        ));

        TypeSubst s2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, alphaVar
        ));

        TypeSubst s3 = TypeSubst.combine(s1, s2);
        assertEquals(deltaVar, s3.get(alphaVar));
        assertEquals(gammaVar, s3.get(betaVar));
    }

    @Test
    public void test07() {

        // --------------------------
        //  v    S2(v)    S1(S2(v))
        // --------------------------
        //  α      β          β
        //  β      ε          δ
        //  γ      α          λ
        //  ε      ε          δ
        // --------------------------
        //
        // s1 = {α ↦ λ, ε ↦ δ}
        // s2 = {α ↦ β, β ↦ ε, γ ↦ α}
        // s1(s2) = combine(s1, s2) = {α ↦ β, β ↦ δ, γ ↦ λ, ε ↦ δ}
        //

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        VarInfr gammaVar = VarInfr.create(PolyInfr.LOWER_GAMMA);
        VarInfr deltaVar = VarInfr.create(PolyInfr.LOWER_DELTA);
        VarInfr epsilonVar = VarInfr.create(PolyInfr.LOWER_EPSILON);
        VarInfr lambdaVar = VarInfr.create(PolyInfr.LOWER_LAMBDA);

        TypeSubst s1 = TypeSubst.create(Map.of(
            alphaVar, lambdaVar,
            epsilonVar, deltaVar
        ));

        TypeSubst s2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, epsilonVar,
            gammaVar, alphaVar
        ));

        TypeSubst s3 = TypeSubst.combine(s1, s2);
        assertEquals(betaVar, s3.get(alphaVar));
        assertEquals(deltaVar, s3.get(betaVar));
        assertEquals(lambdaVar, s3.get(gammaVar));
        assertEquals(deltaVar, s3.get(epsilonVar));
    }

    @Test
    public void test08() {

        // --------------------------------------
        //  v    S2(v)        S1(S2(v))
        // --------------------------------------
        //  α      β              β
        //  β      (γ) -> Bool    (Int32) -> Bool
        //  γ      y              Int32
        // --------------------------------------
        //
        // s1 = {γ ↦ Int32}
        // s2 = {α ↦ β, β ↦ (γ) -> Bool}
        // s1(s2) = combine(s1, s2) = {α ↦ β, β ↦ (Int32) -> Bool, γ ↦ Int32}
        //

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        VarInfr gammaVar = VarInfr.create(PolyInfr.LOWER_GAMMA);

        TypeSubst s1 = TypeSubst.create(Map.of(gammaVar, ScalarInfr.INT32));
        assertEquals("Int32", s1.get(gammaVar).toString());

        TypeSubst s2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, FuncInfr.create(List.of(gammaVar, ScalarInfr.BOOL))
        ));
        assertEquals("β", s2.get(alphaVar).toString());
        assertEquals("(γ) -> Bool", s2.get(betaVar).toString());

        TypeSubst s3 = TypeSubst.combine(s1, s2);
        assertEquals("β", s3.get(alphaVar).toString());
        assertEquals("(Int32) -> Bool", s3.get(betaVar).toString());
        assertEquals("Int32", s3.get(gammaVar).toString());
    }

}
