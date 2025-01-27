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

public class TestEvalLocal {

    @Test
    public void testComplexExpr() throws Exception {
        String source = """
            local a, b, c, d in
                a = 3
                b = 5
                c = local x = 1 in
                    a + x
                end
                local y = 2 in
                    c + b + y
                end
            end""";
        Ident z = Ident.create("z");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(z)
            .setExprIdent(z)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a, b, c, d in
                $bind(3, a)
                $bind(5, b)
                local x = 1 in
                    $add(a, x, c)
                end
                local y = 2 in
                    local $v0 in
                        $add(c, b, $v0)
                        $add($v0, y, z)
                    end
                end
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(11), e.varAtName("z").valueOrVarSet());
    }

    @Test
    public void testComplexStmt() throws Exception {
        String source = """
            local a, b, c, d in
                a = 3
                b = 5
                c = local x = 1 in
                    a + x
                end
                d = local y = 2 in
                    b + y
                end
                z = c + d
            end""";
        Ident z = Ident.create("z");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(z)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a, b, c, d in
                $bind(3, a)
                $bind(5, b)
                local x = 1 in
                    $add(a, x, c)
                end
                local y = 2 in
                    $add(b, y, d)
                end
                $add(c, d, z)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(11), e.varAtName("z").valueOrVarSet());
    }

    @Test
    public void testExpr() throws Exception {
        String source = """
            local x, y in
                x = 3
                y = 5
                x + y
            end""";
        Ident z = Ident.create("z");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(z)
            .setExprIdent(z)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local x, y in
                $bind(3, x)
                $bind(5, y)
                $add(x, y, z)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_8, e.varAtName("z").valueOrVarSet());
    }

    @Test
    public void testStmt() throws Exception {
        String source = """
            local x, y in
                x = 3
                y = 5
                z = x + y
            end""";
        Ident z = Ident.create("z");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(z)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local x, y in
                $bind(3, x)
                $bind(5, y)
                $add(x, y, z)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_8, e.varAtName("z").valueOrVarSet());
    }

}
