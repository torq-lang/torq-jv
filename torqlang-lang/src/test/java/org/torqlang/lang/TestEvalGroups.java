/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Bool;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Int32;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalGroups {

    @Test
    public void test01() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = (1 + 2) * 3")
            .perform();
        assertEquals("x = (1 + 2) * 3", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                $add(1, 2, $v0)
                $mult($v0, 3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(9), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test02() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 2 * (3 + 4) * 5")
            .perform();
        assertEquals("x = 2 * (3 + 4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    $add(3, 4, $v1)
                    $mult(2, $v1, $v0)
                end
                $mult($v0, 5, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(70), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test03() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 2 * (3 + -4) * 5")
            .perform();
        assertEquals("x = 2 * (3 + -4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    $add(3, -4, $v1)
                    $mult(2, $v1, $v0)
                end
                $mult($v0, 5, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(-10), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test04() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = -9 > 2 * (3 + -4) * 5")
            .perform();
        assertEquals("x = -9 > 2 * (3 + -4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    local $v2 in
                        $add(3, -4, $v2)
                        $mult(2, $v2, $v1)
                    end
                    $mult($v1, 5, $v0)
                end
                $gt(-9, $v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.TRUE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test05() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = -11 > 2 * (3 + -4) * 5")
            .perform();
        assertEquals("x = -11 > 2 * (3 + -4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    local $v2 in
                        $add(3, -4, $v2)
                        $mult(2, $v2, $v1)
                    end
                    $mult($v1, 5, $v0)
                end
                $gt(-11, $v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test06() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 2 * (--3 + ---4) * 5")
            .perform();
        assertEquals("x = 2 * (--3 + ---4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    local $v2, $v3 in
                        $negate(-3, $v2)
                        local $v4 in
                            $negate(-4, $v4)
                            $negate($v4, $v3)
                        end
                        $add($v2, $v3, $v1)
                    end
                    $mult(2, $v1, $v0)
                end
                $mult($v0, 5, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(-10), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test07() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = -11 > 2 * (--3 + ---4) * 5")
            .perform();
        assertEquals("x = -11 > 2 * (--3 + ---4) * 5", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                local $v1 in
                    local $v2 in
                        local $v3, $v4 in
                            $negate(-3, $v3)
                            local $v5 in
                                $negate(-4, $v5)
                                $negate($v5, $v4)
                            end
                            $add($v3, $v4, $v2)
                        end
                        $mult(2, $v2, $v1)
                    end
                    $mult($v1, 5, $v0)
                end
                $gt(-11, $v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test08() throws Exception {
        String source = """
            begin
                var a = 5
                x = a + 3
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a = 5 in
                $add(a, 3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(8), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void test08_2() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("(var a = 5; x = a + 3)")
            .perform();
        String expected = """
            (var a = 5
            x = a + 3)""";
        assertEquals(expected, e.stmtOrExpr().toString());
        expected = """
            local a = 5 in
                $add(a, 3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(8), e.varAtName("x").valueOrVarSet());
    }

}
