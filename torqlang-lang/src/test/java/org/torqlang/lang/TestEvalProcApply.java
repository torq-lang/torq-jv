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

public class TestEvalProcApply {

    @Test
    public void testFuncExprApply() throws Exception {
        String source = """
            begin
                var add_2 = func (n) in
                    n + 2
                end
                x = add_2(3)
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        String expected = """
            begin
                var add_2 = func (n) in n + 2 end
                x = add_2(3)
            end""";
        assertEquals(expected, e.stmtOrExpr().toString());
        expected = """
            local add_2 in
                $create_proc(proc (n, $r) in
                    $add(n, 2, $r)
                end, add_2)
                add_2(3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testFuncLambdaApply() throws Exception {
        String source = """
            begin
                x = (func (n) in n + 2 end)(3)
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        String expected = """
            begin
                x = (func (n) in n + 2 end)(3)
            end""";
        assertEquals(expected, e.stmtOrExpr().toString());
        expected = """         
            local $v0 in
                $create_proc(proc (n, $r) in
                    $add(n, 2, $r)
                end, $v0)
                $v0(3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testFuncStmtApply() throws Exception {
        String source = """
            begin
                func add_2(x) in
                    x + 2
                end
                x = add_2(3)
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local add_2 in
                $create_proc(proc (x, $r) in
                    $add(x, 2, $r)
                end, add_2)
                add_2(3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testFuncStmtApplyReturnLiteral() throws Exception {
        String source = """
            begin
                func do_it() in
                    if true then
                        1
                    else
                        2
                    end
                end
                do_it()
            end""";
        Ident x = Ident.create("x");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(x)
            .setExprIdent(x)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local do_it in
                $create_proc(proc ($r) in
                    if true then
                        $bind(1, $r)
                    else
                        $bind(2, $r)
                    end
                end, do_it)
                do_it(x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_1, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testProcExprApply() throws Exception {
        String source = """
            begin
                var add_2 = proc (n, r) in
                    r = n + 2
                end
                add_2(3, x)
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        String expected = """
            begin
                var add_2 = proc (n, r) in r = n + 2 end
                add_2(3, x)
            end""";
        assertEquals(expected, e.stmtOrExpr().toString());
        expected = """
            local add_2 in
                $create_proc(proc (n, r) in
                    $add(n, 2, r)
                end, add_2)
                add_2(3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testProcStmtApply() throws Exception {
        String source = """
            begin
                proc add_2(n, r) in
                    r = n + 2
                end
                add_2(3, x)
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local add_2 in
                $create_proc(proc (n, r) in
                    $add(n, 2, r)
                end, add_2)
                add_2(3, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(5), e.varAtName("x").valueOrVarSet());
    }

}
