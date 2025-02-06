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

        // S1 = {α ↦ Bool}
        // S1((α) -> Bool) = (Int32) -> Bool

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        TypeSubst S1 = TypeSubst.create(Map.of(alphaVar, ScalarType.INT32));

        FuncType type1 = FuncType.create(List.of(alphaVar, ScalarType.BOOL));
        assertEquals(alphaVar, type1.params().get(0));
        assertEquals(ScalarType.BOOL, type1.params().get(1));
        assertEquals("(α) -> Bool", type1.toString());

        FuncType type2 = type1.subst(S1);
        assertEquals(ScalarType.INT32, type2.params().get(0));
        assertEquals(ScalarType.BOOL, type2.params().get(1));
        assertEquals("(Int32) -> Bool", type2.toString());
    }

    @Test
    public void test02() {

        // S1 = {α ↦ β, β ↦ Int32}
        // S1((α) -> β) = (β) -> Int32
        // S1(S1((α) -> β)) = (Int32) -> Int32

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        TypeSubst S1 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, ScalarType.INT32
        ));

        FuncType type1 = FuncType.create(List.of(alphaVar, betaVar));
        assertEquals(alphaVar, type1.params().get(0));
        assertEquals(betaVar, type1.params().get(1));
        assertEquals("(α) -> β", type1.toString());

        FuncType type2 = type1.subst(S1);
        assertEquals(betaVar, type2.params().get(0));
        assertEquals(ScalarType.INT32, type2.params().get(1));
        assertEquals("(β) -> Int32", type2.toString());

        AppType type3 = type2.subst(S1);
        assertEquals(ScalarType.INT32, type3.params().get(0));
        assertEquals(ScalarType.INT32, type3.params().get(1));
        assertEquals("(Int32) -> Int32", type3.toString());
    }

    @Test
    public void test03() {

        // S1 = {α ↦ β, β ↦ Int32}
        // S1((α) -> β) = (β) -> Int32
        // S1(S1((α) -> β)) = (Int32) -> Int32

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        VarType gammaVar = VarType.create(PolyType.LOWER_GAMMA);
        VarType deltaVar = VarType.create(PolyType.LOWER_DELTA);

        TypeSubst S1 = TypeSubst.create(Map.of(
            alphaVar, gammaVar,
            betaVar, deltaVar
        ));

        TypeSubst S2 = TypeSubst.create(Map.of(alphaVar, betaVar));

        TypeSubst S3 = TypeSubst.combine(S1, S2);
        assertEquals(deltaVar, S3.get(alphaVar));
        assertEquals(deltaVar, S3.get(betaVar));
    }

    @Test
    public void test04() {
        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        VarType gammaVar = VarType.create(PolyType.LOWER_GAMMA);
        VarType deltaVar = VarType.create(PolyType.LOWER_DELTA);

        TypeSubst S1 = TypeSubst.create(Map.of(
            alphaVar, gammaVar,
            betaVar, deltaVar
        ));

        TypeSubst S2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, alphaVar
        ));

        TypeSubst S3 = TypeSubst.combine(S1, S2);
        assertEquals(deltaVar, S3.get(alphaVar));
        assertEquals(gammaVar, S3.get(betaVar));
    }

    @Test
    public void test05() {
        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        VarType gammaVar = VarType.create(PolyType.LOWER_GAMMA);
        VarType deltaVar = VarType.create(PolyType.LOWER_DELTA);
        VarType epsilonVar = VarType.create(PolyType.LOWER_EPSILON);
        VarType lambdaVar = VarType.create(PolyType.LOWER_LAMBDA);

        TypeSubst S1 = TypeSubst.create(Map.of(
            alphaVar, lambdaVar,
            epsilonVar, deltaVar
        ));

        TypeSubst S2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, epsilonVar,
            gammaVar, alphaVar
        ));

        TypeSubst S3 = TypeSubst.combine(S1, S2);
        assertEquals(betaVar, S3.get(alphaVar));
        assertEquals(deltaVar, S3.get(betaVar));
        assertEquals(lambdaVar, S3.get(gammaVar));
        assertEquals(deltaVar, S3.get(epsilonVar));
    }

    @Test
    public void test06() {

        // S1 = {γ ↦ Int32}
        // S2 = {α ↦ β, β ↦ (γ) -> Bool}
        // combine(S1, S2) = {α ↦ β, β ↦ (Int32) -> Bool, γ ↦ Int32}

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        VarType gammaVar = VarType.create(PolyType.LOWER_GAMMA);

        TypeSubst S1 = TypeSubst.create(Map.of(gammaVar, ScalarType.INT32));
        assertEquals("Int32", S1.get(gammaVar).toString());

        TypeSubst S2 = TypeSubst.create(Map.of(
            alphaVar, betaVar,
            betaVar, FuncType.create(List.of(gammaVar, ScalarType.BOOL))
        ));
        assertEquals("β", S2.get(alphaVar).toString());
        assertEquals("(γ) -> Bool", S2.get(betaVar).toString());

        TypeSubst S3 = TypeSubst.combine(S1, S2);
        assertEquals("β", S3.get(alphaVar).toString());
        assertEquals("(Int32) -> Bool", S3.get(betaVar).toString());
        assertEquals("Int32", S3.get(gammaVar).toString());
    }

    @Test
    public void test07() {

        // a = (Int32) -> α
        // b = (β) -> Bool
        // S = unify(a, b)
        // S(a) == S(b) == (Int32) -> Bool

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);

        TypeSubst S = TypeSubst.unify(alphaVar, betaVar);
        assertEquals(betaVar, S.get(alphaVar));
        assertNull(S.get(betaVar));
        assertEquals(alphaVar.subst(S), betaVar.subst(S));

        FuncType a = FuncType.create(List.of(ScalarType.INT32, alphaVar));
        FuncType b = FuncType.create(List.of(betaVar, ScalarType.BOOL));
        S = TypeSubst.unify(a, b);
        assertEquals(ScalarType.BOOL, S.get(alphaVar));
        assertEquals(ScalarType.INT32, S.get(betaVar));
        assertEquals(a.subst(S), b.subst(S));
    }
}
