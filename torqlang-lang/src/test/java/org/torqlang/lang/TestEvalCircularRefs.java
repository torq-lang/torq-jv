/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.stripCircularSpecifics;

public class TestEvalCircularRefs {

    @Test
    public void testEmployeeManager() throws Exception {
        String source = """
            begin
                var employee = {'name': 'Bob', 'manager': x}
                x = employee
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local employee in
                $create_rec({'name': 'Bob', 'manager': x}, employee)
                $bind(employee, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(Rec.class, e.varAtName("x").valueOrVarSet());
        Rec x = (Rec) e.varAtName("x").valueOrVarSet();
        assertSame(x, x.findValue(Str.of("manager")).resolveValueOrVar());
    }

    @Test
    public void testIndirectRef() throws Exception {
        String source = """
            begin
                var a = {'next': b}
                var b = {'next': x}
                x = a
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a, b in
                $create_rec({'next': b}, a)
                $create_rec({'next': x}, b)
                $bind(a, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(Rec.class, e.varAtName("x").valueOrVarSet());
        Rec x = (Rec) e.varAtName("x").valueOrVarSet();
        Rec b = (Rec) x.findValue(Str.of("next")).resolveValueOrVar();
        Rec a = (Rec) x.findValue(Str.of("next")).resolveValueOrVar();
        assertNotSame(x, b);
        assertNotSame(x, a);
    }

    @Test
    public void testUnifySame() throws Exception {
        String source = """
            begin
                var a = [x]
                var b = [a]
                a = b
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local a, b in
                $create_tuple([x], a)
                $create_tuple([a], b)
                $bind(b, a)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(Rec.class, e.varAtName("x").valueOrVarSet());
        Rec x = (Rec) e.varAtName("x").valueOrVarSet();
        // The only field in `x` refers to `x`
        assertSame(x, x.valueAt(0).resolveValueOrVar());
        // This is a circular reference
        assertEquals("[<<$circular>>]", stripCircularSpecifics(x.toString()));
    }

}
