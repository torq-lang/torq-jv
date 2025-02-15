/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Int32;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalDotSelectExprs {

    @Test
    public void testSingleSelect() throws Exception {
        String source = """
            begin
                var r = {'f0': 0}
                x = r.f0
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local r in
                $bind({'f0': 0}, r)
                $select(r, 'f0', x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(0), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testDoubleSelect() throws Exception {
        String source = """
            begin
                var r = {'f0': {'f1': 1}}
                x = r.f0.f1
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local r in
                $bind({'f0': {'f1': 1}}, r)
                local $v0 in
                    $select(r, 'f0', $v0)
                    $select($v0, 'f1', x)
                end
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(1), e.varAtName("x").valueOrVarSet());
    }

}
