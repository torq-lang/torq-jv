/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.CellPack;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Var;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalAccessNums {

    @Test
    public void testInt32() throws Exception {
        String source = """
            begin
                var a = Cell.new(5)
                x = @a
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(CellPack.CELL_IDENT, new Var(CellPack.CELL_CLS))
            .addVar(Ident.create("a"))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a in
                $select_apply(Cell, ['new'], 5, a)
                $get(a, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_5, e.varAtName("x").valueOrVarSet());
    }

    @Test
    public void testInt32WithAdd() throws Exception {
        String source = """
            begin
                var a = Cell.new(5)
                x = @a + 3
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(CellPack.CELL_IDENT, new Var(CellPack.CELL_CLS))
            .addVar(Ident.create("a"))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a in
                $select_apply(Cell, ['new'], 5, a)
                local $v0 in
                    $get(a, $v0)
                    $add($v0, 3, x)
                end
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Int32.I32_8, e.varAtName("x").valueOrVarSet());
    }

}
