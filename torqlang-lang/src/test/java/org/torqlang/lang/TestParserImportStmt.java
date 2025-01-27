/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Str;

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
        assertEquals(Str.of("system.module"), importStmt.qualifier);
        assertEquals(1, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testDoubleQualifierMultiSelection() {
        //                         1         2         3
        //               01234567890123456789012345678901234567
        String source = "import system.module[ArrayList, Cell]";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 37);
        assertEquals(Str.of("system.module"), importStmt.qualifier);
        assertEquals(2, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(Str.of("Cell"), importStmt.names.get(1).name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testNoQualifier() {
        //                         1
        //               01234567890123456
        String source = "import ArrayList";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 16);
        assertEquals(Str.of(""), importStmt.qualifier);
        assertEquals(1, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testNoQualifierMultiSelection() {
        //                         1         2
        //               0123456789012345678901234
        String source = "import [ArrayList, Cell]";
        Parser p = new Parser(source);
        ParserError exc = assertThrows(ParserError.class, p::parse);
        assertEquals("Identifier expected", exc.getMessage());
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
        assertEquals(Str.of("system"), importStmt.qualifier);
        assertEquals(1, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testSingleQualifierMultiSelection() {
        //                         1         2         3
        //               0123456789012345678901234567890
        String source = "import system[ArrayList, Cell]";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 30);
        assertEquals(Str.of("system"), importStmt.qualifier);
        assertEquals(2, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(Str.of("Cell"), importStmt.names.get(1).name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());
    }

    @Test
    public void testSingleQualifierMultiSelectionAlias() {
        //                         1         2         3         4
        //               012345678901234567890123456789012345678901234567
        String source = "import system[ArrayList as JavaArrayList, Cell]";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        ImportStmt importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 47);
        assertEquals(Str.of("system"), importStmt.qualifier);
        assertEquals(2, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertEquals(Str.of("JavaArrayList"), importStmt.names.get(0).alias);
        assertEquals(Str.of("Cell"), importStmt.names.get(1).name);
        assertNull(importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());

        //                  1         2         3         4
        //        01234567890123456789012345678901234567890
        source = "import system[ArrayList, Cell as MyCell]";
        p = new Parser("import system[ArrayList, Cell as MyCell]");
        sox = p.parse();
        assertInstanceOf(ImportStmt.class, sox);
        importStmt = (ImportStmt) sox;
        assertSourceSpan(importStmt, 0, 40);
        assertEquals(Str.of("system"), importStmt.qualifier);
        assertEquals(2, importStmt.names.size());
        assertEquals(Str.of("ArrayList"), importStmt.names.get(0).name);
        assertNull(importStmt.names.get(0).alias);
        assertEquals(Str.of("Cell"), importStmt.names.get(1).name);
        assertEquals(Str.of("MyCell"), importStmt.names.get(1).alias);
        assertEquals(source, sox.toString());
    }

}
