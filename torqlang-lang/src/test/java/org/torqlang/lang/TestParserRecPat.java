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

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.*;

public class TestParserRecPat {

    @Test
    public void testEmpty() {
        //                                      1
        //                            01234567890
        Parser p = new Parser("var {} = x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 10);
        assertEquals(1, varStmt.varDecls.size());
        InitVarDecl decl = CommonTools.asInitVarDecl(varStmt.varDecls.get(0));
        assertSourceSpan(decl, 4, 10);
        assertInstanceOf(RecPat.class, decl.varPat);
        RecPat recPat = (RecPat) decl.varPat;
        assertFalse(recPat.partialArity());
        assertSourceSpan(recPat, 4, 6);
        // Test toString format
        String expectedFormat = "var {} = x";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var {} = x";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures1() {
        //                                      1
        //                            012345678901
        Parser p = new Parser("var {a} = x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 11);
        assertEquals(1, varStmt.varDecls.size());
        InitVarDecl decl = CommonTools.asInitVarDecl(varStmt.varDecls.get(0));
        assertSourceSpan(decl, 4, 11);
        assertInstanceOf(RecPat.class, decl.varPat);
        RecPat recPat = (RecPat) decl.varPat;
        assertSourceSpan(recPat, 4, 7);
        // Test label and partial arity
        assertNull(recPat.label());
        assertFalse(recPat.partialArity());
        // Test features
        assertEquals(1, recPat.fields().size());
        FieldPat fieldPat = recPat.fields().get(0);
        assertEquals(Int32.I32_0, asFeatureAsPat(fieldPat.feature).value());
        assertEquals(Ident.create("a"), asIdentAsPat(fieldPat.value).ident);
        // Test toString format
        String expectedFormat = "var {0: a} = x";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var {0: a} = x";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures1PartialArity() {
        //                                      1
        //                            01234567890123456
        Parser p = new Parser("var {a, ...} = x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 16);
        assertEquals(1, varStmt.varDecls.size());
        InitVarDecl decl = CommonTools.asInitVarDecl(varStmt.varDecls.get(0));
        assertSourceSpan(decl, 4, 16);
        assertInstanceOf(RecPat.class, decl.varPat);
        RecPat recPat = (RecPat) decl.varPat;
        assertSourceSpan(recPat, 4, 12);
        // Test label and partial arity
        assertNull(recPat.label());
        assertTrue(recPat.partialArity());
        // Test features
        assertEquals(1, recPat.fields().size());
        FieldPat fieldPat = recPat.fields().get(0);
        assertEquals(Int32.I32_0, asFeatureAsPat(fieldPat.feature).value());
        assertEquals(Ident.create("a"), asIdentAsPat(fieldPat.value).ident);
        // Test toString format
        String expectedFormat = "var {0: a, ...} = x";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var {0: a, ...} = x";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures2() {
        //                                      1
        //                            012345678901234
        Parser p = new Parser("var {a, b} = x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 14);
        assertEquals(1, varStmt.varDecls.size());
        InitVarDecl decl = CommonTools.asInitVarDecl(varStmt.varDecls.get(0));
        assertSourceSpan(decl, 4, 14);
        assertInstanceOf(RecPat.class, decl.varPat);
        RecPat recPat = (RecPat) decl.varPat;
        assertSourceSpan(recPat, 4, 10);
        // Test label and partial arity
        assertNull(recPat.label());
        assertFalse(recPat.partialArity());
        // Test features
        assertEquals(2, recPat.fields().size());
        FieldPat fieldPat = recPat.fields().get(0);
        assertEquals(Int32.I32_0, asFeatureAsPat(fieldPat.feature).value());
        assertEquals(Ident.create("a"), asIdentAsPat(fieldPat.value).ident);
        fieldPat = recPat.fields().get(1);
        assertEquals(Int32.I32_1, asFeatureAsPat(fieldPat.feature).value());
        assertEquals(Ident.create("b"), asIdentAsPat(fieldPat.value).ident);
        // Test toString format
        String expectedFormat = "var {0: a, 1: b} = x";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var {0: a, 1: b} = x";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures2PartialArity() {
        //                                      1         2
        //                            012345678901234567890
        Parser p = new Parser("var {a, ~b, ...} = x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 20);
        assertEquals(1, varStmt.varDecls.size());
        InitVarDecl decl = CommonTools.asInitVarDecl(varStmt.varDecls.get(0));
        assertSourceSpan(decl, 4, 20);
        assertInstanceOf(RecPat.class, decl.varPat);
        RecPat recPat = (RecPat) decl.varPat;
        assertSourceSpan(recPat, 4, 16);
        // Test label and partial arity
        assertNull(recPat.label());
        assertTrue(recPat.partialArity());
        // Test features
        assertEquals(2, recPat.fields().size());
        FieldPat fieldPat = recPat.fields().get(0);
        assertEquals(Int32.I32_0, asFeatureAsPat(fieldPat.feature).value());
        assertEquals(Ident.create("a"), asIdentAsPat(fieldPat.value).ident);
        fieldPat = recPat.fields().get(1);
        assertEquals(Int32.I32_1, asFeatureAsPat(fieldPat.feature).value());
        IdentAsPat bPat = asIdentAsPat(fieldPat.value);
        assertTrue(bPat.escaped);
        assertEquals(Ident.create("b"), bPat.ident);
        assertNull(bPat.typeAnno);
        // Test toString format
        String expectedFormat = "var {0: a, 1: ~b, ...} = x";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var {0: a, 1: ~b, ...} = x";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

}
