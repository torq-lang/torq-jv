/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalLessThanNums {

    @Test
    public void testInt32AndInt32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 7 < 5")
            .perform();
        assertEquals("x = 7 < 5", e.sntcOrExpr().toString());
        assertEquals("$lt(7, 5, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 5 < 5")
            .perform();
        assertEquals("x = 5 < 5", e.sntcOrExpr().toString());
        assertEquals("$lt(5, 5, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testInt32AndVar32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = 7 < a")
            .perform();
        assertEquals("x = 7 < a", e.sntcOrExpr().toString());
        assertEquals("$lt(7, a, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = 5 < a")
            .perform();
        assertEquals("x = 5 < a", e.sntcOrExpr().toString());
        assertEquals("$lt(5, a, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testInt64AndInt64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 7L < 5L")
            .perform();
        assertEquals("x = 7L < 5L", e.sntcOrExpr().toString());
        assertEquals("$lt(7L, 5L, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource("x = 5L < 5L")
            .perform();
        assertEquals("x = 5L < 5L", e.sntcOrExpr().toString());
        assertEquals("$lt(5L, 5L, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testInt64AndVar64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = 7L < a")
            .perform();
        assertEquals("x = 7L < a", e.sntcOrExpr().toString());
        assertEquals("$lt(7L, a, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.I64_5))
            .setSource("x = 5L < a")
            .perform();
        assertEquals("x = 5L < a", e.sntcOrExpr().toString());
        assertEquals("$lt(5L, a, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar32AndInt32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_7))
            .setSource("x = a < 5")
            .perform();
        assertEquals("x = a < 5", e.sntcOrExpr().toString());
        assertEquals("$lt(a, 5, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .setSource("x = a < 5")
            .perform();
        assertEquals("x = a < 5", e.sntcOrExpr().toString());
        assertEquals("$lt(a, 5, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar32AndVar32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_7))
            .addVar(Ident.create("b"), new Var(Int32.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.I32_5))
            .addVar(Ident.create("b"), new Var(Int32.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar32AndVar64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.of(7)))
            .addVar(Ident.create("b"), new Var(Int64.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int32.of(5)))
            .addVar(Ident.create("b"), new Var(Int64.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar64AndInt64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(7)))
            .setSource("x = a < 5L")
            .perform();
        assertEquals("x = a < 5L", e.sntcOrExpr().toString());
        assertEquals("$lt(a, 5L, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(5)))
            .setSource("x = a < 5L")
            .perform();
        assertEquals("x = a < 5L", e.sntcOrExpr().toString());
        assertEquals("$lt(a, 5L, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar64AndVar32() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(7)))
            .addVar(Ident.create("b"), new Var(Int32.of(5)))
            .setSource("x = a < b").perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(5)))
            .addVar(Ident.create("b"), new Var(Int32.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testVar64AndVar64() throws Exception {
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(7)))
            .addVar(Ident.create("b"), new Var(Int64.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());

        e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"), new Var(Int64.of(5)))
            .addVar(Ident.create("b"), new Var(Int64.of(5)))
            .setSource("x = a < b")
            .perform();
        assertEquals("x = a < b", e.sntcOrExpr().toString());
        assertEquals("$lt(a, b, x)", e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

}
