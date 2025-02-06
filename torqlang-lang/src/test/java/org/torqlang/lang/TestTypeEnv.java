/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeEnv {

    @Test
    public void test01() {

        //
        // Γ =
        //    x : β,
        //    y : ((γ) -> Int) -> Int,
        //    z : ∀δ. δ
        //
        // σ = ∀ε. α -> β -> γ -> δ -> ε
        //
        // FV(Γ) = {β, γ}
        // FV(σ) = {α, β, γ, δ}
        // FV(σ) - FV(Γ) = {α, δ}
        //
        // generalize(Γ, σ) = ∀α. ∀δ. ∀ε. α -> β -> γ -> δ -> ε
        //

        VarType alphaVar = VarType.create(PolyType.LOWER_ALPHA);
        VarType betaVar = VarType.create(PolyType.LOWER_BETA);
        VarType gammaVar = VarType.create(PolyType.LOWER_GAMMA);
        VarType deltaVar = VarType.create(PolyType.LOWER_DELTA);
        VarType epsilonVar = VarType.create(PolyType.LOWER_EPSILON);

        QuantType sigma = (QuantType) FuncType
            .create(List.of(alphaVar, betaVar, gammaVar, deltaVar, epsilonVar))
            .addQuantifiers(Set.of(epsilonVar));
        assertEquals("∀ε. (α, β, γ, δ) -> ε", sigma.toString());

        TypeEnv gamma = TypeEnv
            .create(
                Map.of(
                    Ident.create("x"), betaVar,
                    Ident.create("y"), FuncType.create(
                        List.of(
                            FuncType.create(List.of(gammaVar, ScalarType.INT32)), ScalarType.INT32
                        )
                    ),
                    Ident.create("z"), QuantType.create(List.of(deltaVar), deltaVar)
                )
            );
        PolyType generalSigma = gamma.generalize(sigma);
        assertInstanceOf(QuantType.class, generalSigma);
        QuantType sigmaQuant = (QuantType) generalSigma;
        assertEquals(sigmaQuant.quantifiers().size(), 3);
        assertTrue(sigmaQuant.quantifiers().contains(alphaVar));
        assertTrue(sigmaQuant.quantifiers().contains(deltaVar));
        assertTrue(sigmaQuant.quantifiers().contains(epsilonVar));
        assertEquals(sigma.monoType(), sigmaQuant.monoType());
    }
}
