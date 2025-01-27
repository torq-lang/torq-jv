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
import org.torqlang.klvm.Int64;
import org.torqlang.klvm.Var;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalNegativeNums {

    @Test
    public void testInt32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 1")
            .perform();
        assertEquals("x = 1", e.stmtOrExpr().toString());
        assertEquals("$bind(1, x)", e.kernel().toString());
        assertEquals(Int32.I32_1, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = -1")
            .perform();
        assertEquals("x = -1", e.stmtOrExpr().toString());
        assertEquals("$negate(1, x)", e.kernel().toString());
        assertEquals(Int32.of(-1), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = --1")
            .perform();
        assertEquals("x = --1", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                $negate(1, $v0)
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_1, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = ---1")
            .perform();
        assertEquals("x = ---1", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                local $v1 in
                    $negate(1, $v1)
                    $negate($v1, $v0)
                end
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(-1), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = a")
            .perform();
        assertEquals("x = a", e.stmtOrExpr().toString());
        assertEquals("$bind(a, x)", e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = -a")
            .perform();
        assertEquals("x = -a", e.stmtOrExpr().toString());
        assertEquals("$negate(a, x)", e.kernel().toString());
        assertEquals(Int32.of(-5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = --a")
            .perform();
        assertEquals("x = --a", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                $negate(a, $v0)
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = ---a")
            .perform();
        assertEquals("x = ---a", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                local $v1 in
                    $negate(a, $v1)
                    $negate($v1, $v0)
                end
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(-5), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testInt64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 1L")
            .perform();
        assertEquals("x = 1L", e.stmtOrExpr().toString());
        assertEquals("$bind(1L, x)", e.kernel().toString());
        assertEquals(Int64.I64_1, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = -1L")
            .perform();
        assertEquals("x = -1L", e.stmtOrExpr().toString());
        assertEquals("$negate(1L, x)", e.kernel().toString());
        assertEquals(Int64.of(-1), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = --1L")
            .perform();
        assertEquals("x = --1L", e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                $negate(1L, $v0)
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int64.I64_1, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = ---1L")
            .perform();
        assertEquals("x = ---1L", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                local $v1 in
                    $negate(1L, $v1)
                    $negate($v1, $v0)
                end
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int64.of(-1), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = a")
            .perform();
        assertEquals("x = a", e.stmtOrExpr().toString());
        assertEquals("$bind(a, x)", e.kernel().toString());
        assertEquals(Int64.of(5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = -a")
            .perform();
        assertEquals("x = -a", e.stmtOrExpr().toString());
        assertEquals("$negate(a, x)", e.kernel().toString());
        assertEquals(Int64.of(-5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = --a")
            .perform();
        assertEquals("x = --a", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                $negate(a, $v0)
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int64.of(5), e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = ---a")
            .perform();
        assertEquals("x = ---a", e.stmtOrExpr().toString());
        expected = """
            local $v0 in
                local $v1 in
                    $negate(a, $v1)
                    $negate($v1, $v0)
                end
                $negate($v0, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int64.of(-5), e.varAtName("x").valueOrVarSet());
    }

}
