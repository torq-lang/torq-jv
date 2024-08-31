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

public class TestParserVarSntc {

    @Test
    public void testWithInit() {
        //                            0123456789
        Parser p = new Parser("var x = 1");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(VarSntc.class, sox);
        VarSntc varSntc = (VarSntc) sox;
        assertSourceSpan(varSntc, 0, 9);
        assertEquals(1, varSntc.varDecls.size());
        assertInstanceOf(InitVarDecl.class, varSntc.varDecls.get(0));
        InitVarDecl initVarDecl = (InitVarDecl) varSntc.varDecls.get(0);
        assertSourceSpan(initVarDecl, 4, 9);
        assertSourceSpan(initVarDecl.varPat, 4, 5);
        assertEquals(Ident.create("x"), asIdentAsPat(initVarDecl.varPat).ident);
        assertEquals(Int32.I32_1, asIntAsExpr(initVarDecl.valueExpr).int64());
        assertSourceSpan(initVarDecl.valueExpr, 8, 9);
        // Test toString format
        String expectedFormat = "var x = 1";
        String actualFormat = varSntc.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var x = 1";
        actualFormat = LangFormatter.SINGLETON.format(varSntc);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testWithTypeAnno() {
        //                                      1
        //                            0123456789012
        Parser p = new Parser("var x::Int32");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(VarSntc.class, sox);
        VarSntc varSntc = (VarSntc) sox;
        assertSourceSpan(varSntc, 0, 12);
        assertEquals(1, varSntc.varDecls.size());
        assertInstanceOf(IdentVarDecl.class, varSntc.varDecls.get(0));
        IdentVarDecl identVarDecl = (IdentVarDecl) varSntc.varDecls.get(0);
        assertSourceSpan(identVarDecl, 4, 12);
        assertSourceSpan(identVarDecl.identAsPat, 4, 12);
        assertEquals(Ident.create("x"), identVarDecl.identAsPat.ident);
        assertFalse(identVarDecl.identAsPat.escaped);
        assertEquals(Ident.create("Int32"), identVarDecl.identAsPat.typeAnno.ident);
        assertSourceSpan(identVarDecl.identAsPat.typeAnno, 7, 12);
        // Test toString format
        String expectedFormat = "var x::Int32";
        String actualFormat = varSntc.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "var x::Int32";
        actualFormat = LangFormatter.SINGLETON.format(varSntc);
        assertEquals(expectedFormat, actualFormat);
    }

}
