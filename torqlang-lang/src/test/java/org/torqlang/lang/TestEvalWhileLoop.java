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

public class TestEvalWhileLoop {

    @Test
    public void testConditionWithCounter() throws Exception {

        // a = 5

        String source = """
            begin
                var c = new Cell(0)
                while @c < a do
                    c := @c + 1
                end
                x = @c
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(CellMod.CELL_IDENT, new Var(CellMod.singleton().namesake()))
            .addVar(Ident.create("a"), new Var(Int32.of(5)))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local c in
                $select_apply(Cell, ['$new'], 0, c)
                local $guard, $while in
                    $create_proc(proc ($r) in // free vars: a, c
                        local $v0 in
                            $get(c, $v0)
                            $lt($v0, a, $r)
                        end
                    end, $guard)
                    $create_proc(proc () in // free vars: $guard, $while, c
                        local $v1 in
                            $guard($v1)
                            if $v1 then
                                local $v2 in
                                    local $v3 in
                                        $get(c, $v3)
                                        $add($v3, 1, $v2)
                                    end
                                    $set(c, $v2)
                                end
                                $while()
                            end
                        end
                    end, $while)
                    $while()
                end
                $get(c, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_5, e.varAtName("x").valueOrVarSet());

        // a = 3

        e = Evaluator.builder()
            .addVar(CellMod.CELL_IDENT, new Var(CellMod.singleton().namesake()))
            .addVar(Ident.create("a"), new Var(Int32.of(3)))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(Int32.I32_3, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testWithBreak() throws Exception {

        // a = 5

        String source = """
            begin
                var c = new Cell(0)
                while true do
                    if @c >= a then
                        break
                    end
                    c := @c + 1
                end
                x = @c
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(CellMod.CELL_IDENT, new Var(CellMod.singleton().namesake()))
            .addVar(Ident.create("a"), new Var(Int32.of(5)))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local c in
                $select_apply(Cell, ['$new'], 0, c)
                local $guard, $while in
                    $create_proc(proc ($r) in
                        $bind(true, $r)
                    end, $guard)
                    $create_proc(proc () in // free vars: $guard, $while, a, c
                        local $v0 in
                            $guard($v0)
                            if $v0 then
                                local $v1 in
                                    local $v2 in
                                        $get(c, $v2)
                                        $ge($v2, a, $v1)
                                    end
                                    if $v1 then
                                        $jump_throw(1)
                                    end
                                    local $v3 in
                                        local $v4 in
                                            $get(c, $v4)
                                            $add($v4, 1, $v3)
                                        end
                                        $set(c, $v3)
                                    end
                                    $while()
                                end
                            end
                        end
                    end, $while)
                    $while()
                    $jump_catch(1)
                end
                $get(c, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_5, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testWithBreakAndContinue() throws Exception {

        // break at 2

        /*
         * If `continue` failed, the answer would be 12, not 13
         * If `break` failed, the loop will not terminate.
         */

        String source = """
            begin
                var i = new Cell(-1)
                var c = new Cell(0)
                while true do
                    i := @i + 1
                    c := @c + 1
                    if @i == 1 then
                        c := @c + 10
                        continue
                    end
                    if @i == 1 then
                        break
                    end
                    if @i == 2 then
                        break
                    end
                end
                x = @c
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(CellMod.CELL_IDENT, new Var(CellMod.singleton().namesake()))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local i, c in
                $select_apply(Cell, ['$new'], -1, i)
                $select_apply(Cell, ['$new'], 0, c)
                local $guard, $while in
                    $create_proc(proc ($r) in
                        $bind(true, $r)
                    end, $guard)
                    $create_proc(proc () in // free vars: $guard, $while, c, i
                        local $v0 in
                            $guard($v0)
                            if $v0 then
                                local $v5, $v9, $v11 in
                                    local $v1 in
                                        local $v2 in
                                            $get(i, $v2)
                                            $add($v2, 1, $v1)
                                        end
                                        $set(i, $v1)
                                    end
                                    local $v3 in
                                        local $v4 in
                                            $get(c, $v4)
                                            $add($v4, 1, $v3)
                                        end
                                        $set(c, $v3)
                                    end
                                    local $v6 in
                                        $get(i, $v6)
                                        $eq($v6, 1, $v5)
                                    end
                                    if $v5 then
                                        local $v7 in
                                            local $v8 in
                                                $get(c, $v8)
                                                $add($v8, 10, $v7)
                                            end
                                            $set(c, $v7)
                                        end
                                        $jump_throw(2)
                                    end
                                    local $v10 in
                                        $get(i, $v10)
                                        $eq($v10, 1, $v9)
                                    end
                                    if $v9 then
                                        $jump_throw(1)
                                    end
                                    local $v12 in
                                        $get(i, $v12)
                                        $eq($v12, 2, $v11)
                                    end
                                    if $v11 then
                                        $jump_throw(1)
                                    end
                                    $jump_catch(2)
                                    $while()
                                end
                            end
                        end
                    end, $while)
                    $while()
                    $jump_catch(1)
                end
                $get(c, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.of(13), e.varAtName("x").valueOrVarSet());
    }

}
