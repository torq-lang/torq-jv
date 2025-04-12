/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserImportStmt {

    @Test
    public void testDoubleQualifier() {
        //                         1         2         3
        //               0123456789012345678901234567890
        String source = "import system.module.ArrayList";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 30);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals("module", importStmt.qualifier.get(1).ident.name);
        assertEquals(1, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testDoubleQualifierMultiSelection() {
        //                         1         2         3
        //               012345678901234567890123456789012345678
        String source = "import system.module.{ArrayList, Cell}";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 38);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals("module", importStmt.qualifier.get(1).ident.name);
        assertEquals(2, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals("Cell", importStmt.names.get(1).name.ident.name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testSingleQualifier() {
        //                         1         2
        //               012345678901234567890123
        String source = "import system.ArrayList";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 23);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals(1, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testSingleQualifierMultiSelection() {
        //                         1         2         3
        //               01234567890123456789012345678901
        String source = "import system.{ArrayList, Cell}";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 31);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals(2, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals("Cell", importStmt.names.get(1).name.ident.name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testSingleQualifierMultiSelectionAlias() {
        //                         1         2         3         4
        //               0123456789012345678901234567890123456789012345678
        String source = "import system.{ArrayList as JavaArrayList, Cell}";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 48);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals(2, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertEquals("JavaArrayList", importStmt.names.get(0).alias.ident.name);
        assertEquals("Cell", importStmt.names.get(1).name.ident.name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());

        //                  1         2         3         4
        //        012345678901234567890123456789012345678901
        source = "import system.{ArrayList, Cell as MyCell}";
        p = new Parser(source);
        sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 41);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals(2, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals("Cell", importStmt.names.get(1).name.ident.name);
        assertEquals("MyCell", importStmt.names.get(1).alias.ident.name);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testImportAndOneExpr() {
        //          1         2         3         4
        //012345678901234567890123456789012345678901
        //begin     import system.ArrayList     new ArrayList() end
        String source = """
            begin
                import system.ArrayList
                new ArrayList()
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;
        assertInstanceOf(ImportStmt.class, beginLang.body.list.get(0));
        ImportStmt importStmt = (ImportStmt) beginLang.body.list.get(0);
        assertSourceSpan(importStmt, 10, 33);
        assertEquals("system", importStmt.qualifier.get(0).ident.name);
        assertEquals(1, importStmt.names.size());
        assertEquals("ArrayList", importStmt.names.get(0).name.ident.name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

}
