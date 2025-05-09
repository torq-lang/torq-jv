/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.junit.jupiter.api.Test;
import org.torqlang.util.SourceSpan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateRecInstr {

    @Test
    public void testCaptureLexicallyFree() {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();

        CreateRecInstr cr;
        Str testLabel = Str.of("test-label");
        Ident x = Ident.create("x");
        RecDef recDef;

        Set<Ident> knownBound;
        Set<Ident> lexicallyFree;

        Str a = Str.of("a");
        recDef = new RecDef(testLabel, List.of(new FieldDef(Int32.I32_0, a, emptySpan)), emptySpan);
        assertEquals("'test-label'#{0: 'a'}", recDef.toString());
        cr = new CreateRecInstr(x, recDef, emptySpan);

        knownBound = new HashSet<>();
        lexicallyFree = new HashSet<>();
        cr.captureLexicallyFree(knownBound, lexicallyFree);
        assertEquals(0, knownBound.size());
        assertEquals(1, lexicallyFree.size());
        assertEquals(x, lexicallyFree.iterator().next());

        knownBound = new HashSet<>();
        knownBound.add(x);
        lexicallyFree = new HashSet<>();
        cr.captureLexicallyFree(knownBound, lexicallyFree);
        assertEquals(1, knownBound.size());
        assertEquals(x, knownBound.iterator().next());
        assertEquals(0, lexicallyFree.size());
    }

    @Test
    public void testSingleCompleteRec() throws Exception {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();
        Str testLabel = Str.of("test-label");
        Ident x = Ident.create("x");
        Var xVar = new Var();
        Str a = Str.of("a");

        Env env = Env.create(null, List.of(new EnvEntry(x, xVar)));
        RecDef recDef = new RecDef(testLabel, List.of(new FieldDef(Int32.I32_0, a, emptySpan)), emptySpan);
        assertEquals("'test-label'#{0: 'a'}", recDef.toString());
        CreateRecInstr cr = new CreateRecInstr(x, recDef, emptySpan);
        cr.compute(env, null);
        assertInstanceOf(CompleteRec.class, xVar.valueOrVarSet());
        CompleteRec rec = (CompleteRec) xVar.resolveValue();
        assertEquals(a, rec.select(Int32.I32_0));
    }

    /*
     * This test validates an important optimization. When record definitions refer to other record
     * definitions, records are created to directly reference each other without using variables.
     */
    @Test
    public void testDoubleCompleteRec() throws Exception {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();
        Str a = Str.of("a");

        Ident x = Ident.create("x");
        Var xVar = new Var();
        Ident y = Ident.create("y");
        Var yVar = new Var();

        // Knit two complete records together in memory
        //     x = 'test-label-1'#{0: 'a'}
        //     y = 'test-label-2'#{0: x}
        // Both x and y are instances of CompleteRec

        Env env = Env.create(null, List.of(new EnvEntry(x, xVar), new EnvEntry(y, yVar)));

        Str testLabel1 = Str.of("test-label-1");
        RecDef xRecDef = new RecDef(testLabel1, List.of(new FieldDef(Int32.I32_0, a, emptySpan)), emptySpan);
        assertEquals("'test-label-1'#{0: 'a'}", xRecDef.toString());
        CreateRecInstr cr1 = new CreateRecInstr(x, xRecDef, emptySpan);
        cr1.compute(env, null);

        Str testLabel2 = Str.of("test-label-2");
        RecDef yRecDef = new RecDef(testLabel2, List.of(new FieldDef(Int32.I32_0, x, emptySpan)), emptySpan);
        assertEquals("'test-label-2'#{0: x}", yRecDef.toString());
        @SuppressWarnings("SuspiciousNameCombination")
        CreateRecInstr cr2 = new CreateRecInstr(y, yRecDef, emptySpan);
        cr2.compute(env, null);

        assertInstanceOf(CompleteRec.class, yVar.valueOrVarSet());
        CompleteRec yRec = (CompleteRec) yVar.valueOrVarSet();
        assertInstanceOf(CompleteRec.class, yRec.select(Int32.I32_0));
        CompleteRec xRec = (CompleteRec) yRec.select(Int32.I32_0);
        assertEquals(Int32.I32_0, xRec.featureAt(0));
        assertEquals(a, xRec.valueAt(0));
    }

    /*
     * This test validates an important optimization. When record definitions refer to other record
     * definitions, records are created to directly reference each other without using variables.
     */
    @Test
    public void testDoublePartialRec() throws Exception {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();
        Str a = Str.of("a");

        Ident x = Ident.create("x");
        Var xVar = new Var();
        Ident y = Ident.create("y");
        Var yVar = new Var();
        Ident z = Ident.create("z");
        Var zVar = new Var();

        // Knit two complete records together in memory
        //     x = 'test-label-1'#{0: 'a', 1: z}
        //     y = 'test-label-2'#{0: x}
        // Both x and y are instances of PartialRec

        Env env = Env.create(null, List.of(new EnvEntry(x, xVar), new EnvEntry(y, yVar), new EnvEntry(z, zVar)));

        Str testLabel1 = Str.of("test-label-1");
        RecDef xRecDef = new RecDef(testLabel1, List.of(new FieldDef(Int32.I32_0, a, emptySpan),
            new FieldDef(Int32.I32_1, z, emptySpan)), emptySpan);
        assertEquals("'test-label-1'#{0: 'a', 1: z}", xRecDef.toString());
        CreateRecInstr cr1 = new CreateRecInstr(x, xRecDef, emptySpan);
        cr1.compute(env, null);

        Str testLabel2 = Str.of("test-label-2");
        RecDef yRecDef = new RecDef(testLabel2, List.of(new FieldDef(Int32.I32_0, x, emptySpan)), emptySpan);
        assertEquals("'test-label-2'#{0: x}", yRecDef.toString());
        @SuppressWarnings("SuspiciousNameCombination")
        CreateRecInstr cr2 = new CreateRecInstr(y, yRecDef, emptySpan);
        cr2.compute(env, null);

        assertInstanceOf(PartialRec.class, yVar.valueOrVarSet());
        PartialRec yRec = (PartialRec) yVar.valueOrVarSet();
        // yRec should directly reference xRec
        assertInstanceOf(PartialRec.class, yRec.valueAt(0));
        PartialRec xRec = (PartialRec) yRec.valueAt(0);
        assertEquals(Int32.I32_0, xRec.featureAt(0));
        assertEquals(a, xRec.valueAt(0));
        assertEquals(Int32.I32_1, xRec.featureAt(1));
        assertEquals(zVar, xRec.valueAt(1));
    }

    @Test
    public void testSinglePartialRec() throws Exception {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();
        Str testLabel = Str.of("test-label");
        Ident x = Ident.create("x");
        Var xVar = new Var();
        Ident y = Ident.create("y");
        Var yVar = new Var();

        Env env = Env.create(null, List.of(new EnvEntry(x, xVar), new EnvEntry(y, yVar)));
        RecDef recDef = new RecDef(testLabel, List.of(new FieldDef(Int32.I32_0, y, emptySpan)), emptySpan);
        assertEquals("'test-label'#{0: y}", recDef.toString());
        CreateRecInstr cr = new CreateRecInstr(x, recDef, emptySpan);
        cr.compute(env, null);
        assertInstanceOf(PartialRec.class, xVar.valueOrVarSet());
        PartialRec rec = (PartialRec) xVar.resolveValue();
        assertEquals(yVar, rec.select(Int32.I32_0));
    }

    @Test
    public void testToKernelString() {

        final SourceSpan emptySpan = SourceSpan.emptySourceSpan();

        CreateRecInstr cr;
        Str testLabel = Str.of("test-label");
        Ident x = Ident.create("x");
        RecDef recDef;

        String expectedToString;

        Str a = Str.of("a");
        recDef = new RecDef(testLabel, List.of(new FieldDef(Int32.I32_0, a, emptySpan)), emptySpan);
        assertEquals("'test-label'#{0: 'a'}", recDef.toString());
        cr = new CreateRecInstr(x, recDef, emptySpan);

        expectedToString = "$create_rec('test-label'#{0: 'a'}, x)";
        assertEquals(expectedToString, cr.toKernelString());
    }

}
