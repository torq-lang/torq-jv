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

public class TestParserVarStmt {

    @Test
    public void testWithInit() {
        //                            0123456789
        Parser p = new Parser("var x = 1");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 9);
        assertEquals(1, varStmt.varDecls.size());
        assertInstanceOf(InitVarDecl.class, varStmt.varDecls.get(0));
        InitVarDecl initVarDecl = (InitVarDecl) varStmt.varDecls.get(0);
        assertSourceSpan(initVarDecl, 4, 9);
        assertSourceSpan(initVarDecl.varPat, 4, 5);
        assertEquals(Ident.create("x"), asIdentAsPat(initVarDecl.varPat).ident);
        assertEquals(Int32.I32_1, asInt64AsExpr(initVarDecl.valueExpr).int64());
        assertSourceSpan(initVarDecl.valueExpr, 8, 9);
        // Test toString format
        String expectedFormat = "var x = 1";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var x = 1";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testWithTypeAnno() {
        //                                      1
        //                            0123456789012
        Parser p = new Parser("var x::Int32");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(VarStmt.class, sox);
        VarStmt varStmt = (VarStmt) sox;
        assertSourceSpan(varStmt, 0, 12);
        assertEquals(1, varStmt.varDecls.size());
        assertInstanceOf(IdentVarDecl.class, varStmt.varDecls.get(0));
        IdentVarDecl identVarDecl = (IdentVarDecl) varStmt.varDecls.get(0);
        assertSourceSpan(identVarDecl, 4, 12);
        assertSourceSpan(identVarDecl.identAsPat, 4, 12);
        assertEquals(Ident.create("x"), identVarDecl.identAsPat.ident);
        assertFalse(identVarDecl.identAsPat.escaped);
        assertEquals(Ident.create("Int32"), asIdentAsType(identVarDecl.identAsPat.type).typeIdent());
        assertSourceSpan(identVarDecl.identAsPat.type, 7, 12);
        // Test toString format
        String expectedFormat = "var x::Int32";
        String actualFormat = varStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var x::Int32";
        actualFormat = LangFormatter.DEFAULT.format(varStmt);
        assertEquals(expectedFormat, actualFormat);
    }

}
