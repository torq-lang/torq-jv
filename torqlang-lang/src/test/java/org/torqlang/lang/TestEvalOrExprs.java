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
import org.torqlang.klvm.Var;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalOrExprs {

    @Test
    public void testFalseOrFalse() throws Exception {
        String source = """
            begin
                x = false || false
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_3))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            if false then
                $bind(true, x)
            else
                $bind(false, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testFalseOrTrue() throws Exception {
        String source = """
            begin
                x = false || true
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_3))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            if false then
                $bind(true, x)
            else
                $bind(true, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.TRUE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testTrueOrFalse() throws Exception {
        String source = """
            begin
                x = true || false
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_3))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            if true then
                $bind(true, x)
            else
                $bind(false, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.TRUE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testTrueOrTrue() throws Exception {
        String source = """
            begin
                x = true || true
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_3))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            if true then
                $bind(true, x)
            else
                $bind(true, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.TRUE, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testWithRelationalOperands() throws Exception {

        // a = 3

        String source = """
            begin
                x = a < 5 || a > 11
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_3))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local $v0 in
                $lt(a, 5, $v0)
                if $v0 then
                    $bind(true, x)
                else
                    $gt(a, 11, x)
                end
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Bool.TRUE, e.varAtName("x").valueOrVarSet());

        // a = 7

        e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Int32.I32_7))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(Bool.FALSE, e.varAtName("x").valueOrVarSet());
    }

}
