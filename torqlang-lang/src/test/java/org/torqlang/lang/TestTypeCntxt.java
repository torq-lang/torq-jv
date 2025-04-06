/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeCntxt {

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

        VarInfr alphaVar = VarInfr.create(PolyInfr.LOWER_ALPHA);
        VarInfr betaVar = VarInfr.create(PolyInfr.LOWER_BETA);
        VarInfr gammaVar = VarInfr.create(PolyInfr.LOWER_GAMMA);
        VarInfr deltaVar = VarInfr.create(PolyInfr.LOWER_DELTA);
        VarInfr epsilonVar = VarInfr.create(PolyInfr.LOWER_EPSILON);

        QuantInfr sigma = (QuantInfr) FuncInfr
            .create(List.of(alphaVar, betaVar, gammaVar, deltaVar, epsilonVar))
            .addQuantifiers(Set.of(epsilonVar));
        assertEquals("∀ε. (α, β, γ, δ) -> ε", sigma.toString());

        TypeCntxt gamma = TypeCntxt
            .create(
                Map.of(
                    new IdentAsExpr(Ident.create("x"), SourceSpan.emptySourceSpan()), betaVar,
                    new IdentAsExpr(Ident.create("y"), SourceSpan.emptySourceSpan()), FuncInfr.create(
                        List.of(
                            FuncInfr.create(List.of(gammaVar, ScalarInfr.INT32)), ScalarInfr.INT32
                        )
                    ),
                    new IdentAsExpr(Ident.create("z"), SourceSpan.emptySourceSpan()), QuantInfr.create(List.of(deltaVar), deltaVar)
                )
            );
        PolyInfr generalSigma = gamma.generalize(sigma);
        assertInstanceOf(QuantInfr.class, generalSigma);
        QuantInfr sigmaQuant = (QuantInfr) generalSigma;
        assertEquals(sigmaQuant.quantifiers().size(), 3);
        assertTrue(sigmaQuant.quantifiers().contains(alphaVar));
        assertTrue(sigmaQuant.quantifiers().contains(deltaVar));
        assertTrue(sigmaQuant.quantifiers().contains(epsilonVar));
        assertEquals(sigma.monoType(), sigmaQuant.monoType());
    }
}
