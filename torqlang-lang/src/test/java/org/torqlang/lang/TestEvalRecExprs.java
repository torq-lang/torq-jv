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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEvalRecExprs {

    @Test
    public void testLabelAndRec() throws Exception {
        String source = """
            begin
                x = 'my-label'#{'01-label': 'my-01-value'}
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = "$bind('my-label'#{'01-label': 'my-01-value'}, x)";
        assertEquals(expected, e.kernel().toString());
        CompleteRec expectedRec = Rec.completeRecBuilder()
            .setLabel(Str.of("my-label"))
            .addField(Str.of("01-label"), Str.of("my-01-value"))
            .build();
        assertTrue(expectedRec.entails((Value) e.varAtName("x").valueOrVarSet(), null));
    }

    @Test
    public void testLabelAndTuple() throws Exception {
        String source = """
            begin
                x = 'my-label'#['my-01-value']
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = "$bind('my-label'#['my-01-value'], x)";
        assertEquals(expected, e.kernel().toString());
        CompleteTuple expectedTuple = Rec.completeTupleBuilder()
            .setLabel(Str.of("my-label"))
            .addValue(Str.of("my-01-value"))
            .build();
        assertTrue(expectedTuple.entails((Value) e.varAtName("x").valueOrVarSet(), null));
    }

    @Test
    public void testNoLabelAndRec() throws Exception {
        String source = """
            begin
                x = {'01-label': 'my-01-value'}
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = "$bind({'01-label': 'my-01-value'}, x)";
        assertEquals(expected, e.kernel().toString());
        CompleteRec expectedRec = Rec.completeRecBuilder()
            .addField(Str.of("01-label"), Str.of("my-01-value"))
            .build();
        assertTrue(expectedRec.entails((Value) e.varAtName("x").valueOrVarSet(), null));
    }

    @Test
    public void testNoLabelAndTuple() throws Exception {
        String source = """
            begin
                x = ['my-01-value']
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = "$bind(['my-01-value'], x)";
        assertEquals(expected, e.kernel().toString());
        CompleteTuple expectedTuple = Rec.completeTupleBuilder()
            .addValue(Str.of("my-01-value"))
            .build();
        assertTrue(expectedTuple.entails((Value) e.varAtName("x").valueOrVarSet(), null));
    }

    @Test
    public void testNoLabelAndPartialRec() throws Exception {
        String source = """
            begin
                var a
                x = {'01-label': a}
                a = 'my-01-value'
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a in
                $create_rec({'01-label': a}, x)
                $bind('my-01-value', a)
            end""";
        assertEquals(expected, e.kernel().toString());
        CompleteRec expectedRec = Rec.completeRecBuilder()
            .addField(Str.of("01-label"), Str.of("my-01-value"))
            .build();
        assertTrue(expectedRec.entails((Value) e.varAtName("x").valueOrVarSet(), null));
    }

}
