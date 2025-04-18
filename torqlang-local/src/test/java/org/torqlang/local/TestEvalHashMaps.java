/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;
import org.torqlang.lang.Evaluator;
import org.torqlang.lang.EvaluatorPerformed;
import org.torqlang.local.HashMapPack.HashMapObj;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.local.CommonTools.stripCircularSpecifics;

public class TestEvalHashMaps {

    @Test
    public void test() throws Exception {
        String source = """
            begin
                x = HashMap.new()
                x.put(['one', 'two'], 'My key is a record!')
                y = ['one', 'two']
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(HashMapPack.HASH_MAP_IDENT, new Var(HashMapPack.HASH_MAP_CLS))
            .addVar(Ident.create("x"))
            .addVar(Ident.create("y"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            $select_apply(HashMap, ['new'], x)
            local $v0 in
                $bind(['one', 'two'], $v0)
                $select_apply(x, ['put'], $v0, 'My key is a record!')
            end
            $bind(['one', 'two'], y)""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(HashMapObj.class, e.varAtName("x").valueOrVarSet());
        HashMapObj x = (HashMapObj) e.varAtName("x").valueOrVarSet();
        assertInstanceOf(Rec.class, e.varAtName("y").valueOrVarSet());
        CompleteRec y = (CompleteRec) e.varAtName("y").valueOrVarSet();
        assertEquals(Str.of("My key is a record!"), x.state().get(y));
    }

    @Test
    public void testIndirectRefWithEquals() throws Exception {
        String source = """
            begin
                a = {'next': b}
                b = {'next': a}
                x = HashMap.new()
                x.put(a, 'My key is circular!')
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(HashMapPack.HASH_MAP_IDENT, new Var(HashMapPack.HASH_MAP_CLS))
            .addVar(Ident.create("x"))
            .addVar(Ident.create("a"))
            .addVar(Ident.create("b"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            $create_rec({'next': b}, a)
            $create_rec({'next': a}, b)
            $select_apply(HashMap, ['new'], x)
            $select_apply(x, ['put'], a, 'My key is circular!')""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(HashMapObj.class, e.varAtName("x").valueOrVarSet());
        HashMapObj x = (HashMapObj) e.varAtName("x").valueOrVarSet();
        CompleteRec completeRec = (CompleteRec) x.state().keySet().iterator().next();
        assertInstanceOf(Rec.class, e.varAtName("a").valueOrVarSet());
        Rec a = (Rec) e.varAtName("a").valueOrVarSet();
        assertInstanceOf(Rec.class, e.varAtName("b").valueOrVarSet());
        Rec b = (Rec) e.varAtName("b").valueOrVarSet();
        // This test contains two partial records that reference each other, `a` and `b`.
        // Record `a` was used as a key, which caused it to be converted to a complete record.
        // Ultimately, the key to the hash map, value `a`, and value `b` are all equal in value.
        assertNotEquals(a, b);
        assertTrue(completeRec.entails(a, null));
        assertTrue(completeRec.entails(b, null));
        assertEquals(x.state().get(b.checkComplete()), Str.of("My key is circular!"));
        // Circular references specially formatted
        assertEquals("{'next': {'next': <<$circular>>}}", stripCircularSpecifics(a.toString()));
    }

    @Test
    public void testValueIter() throws Exception {
        String source = """
            begin
                var hm = HashMap.new()
                hm.put('0-key', 'Zero')
                hm.put('1-key', 'One')
                var value_iter = ValueIter.new(hm)
                x = value_iter()
                y = value_iter()
                z = value_iter()
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(HashMapPack.HASH_MAP_IDENT, new Var(HashMapPack.HASH_MAP_CLS))
            .addVar(ValueIterPack.VALUE_ITER_IDENT, new Var(ValueIterPack.VALUE_ITER_CLS))
            .addVar(Ident.create("x"))
            .addVar(Ident.create("y"))
            .addVar(Ident.create("z"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local hm, value_iter in
                $select_apply(HashMap, ['new'], hm)
                $select_apply(hm, ['put'], '0-key', 'Zero')
                $select_apply(hm, ['put'], '1-key', 'One')
                $select_apply(ValueIter, ['new'], hm, value_iter)
                value_iter(x)
                value_iter(y)
                value_iter(z)
            end""";
        assertEquals(expected, e.kernel().toString());
        ValueOrVar x = e.varAtName("x").resolveValueOrVar();
        assertTrue(x.equals(Str.of("Zero")) || x.equals(Str.of("One")));
        ValueOrVar y = e.varAtName("y").resolveValueOrVar();
        assertTrue(y.equals(Str.of("Zero")) || y.equals(Str.of("One")));
        ValueOrVar z = e.varAtName("z").resolveValueOrVar();
        assertEquals(Eof.SINGLETON, z);
    }

}