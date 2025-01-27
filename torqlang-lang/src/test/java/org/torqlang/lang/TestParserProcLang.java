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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.*;

public class TestParserProcLang {

    @Test
    public void testExpr() {
        //                                      1
        //                            01234567890123456
        Parser p = new Parser("proc () in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcExpr.class, sox);
        ProcExpr procExpr = (ProcExpr) sox;
        assertSourceSpan(procExpr, 0, 16);
        assertSourceSpan(procExpr.body, 11, 12);
        assertEquals(0, procExpr.formalArgs.size());
        assertEquals(1, procExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc () in
                0
            end""";
        String actualFormat = procExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testExprWithArgs1() {
        //                                      1
        //                            012345678901234567
        Parser p = new Parser("proc (a) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcExpr.class, sox);
        ProcExpr procExpr = (ProcExpr) sox;
        assertSourceSpan(procExpr, 0, 17);
        assertSourceSpan(procExpr.body, 12, 13);
        assertEquals(1, procExpr.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(procExpr.formalArgs.get(0)).ident);
        assertEquals(1, procExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc (a) in
                0
            end""";
        String actualFormat = procExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testExprWithArgs2() {
        //                                      1         2
        //                            012345678901234567890
        Parser p = new Parser("proc (a, b) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcExpr.class, sox);
        ProcExpr procExpr = (ProcExpr) sox;
        assertSourceSpan(procExpr, 0, 20);
        assertSourceSpan(procExpr.body, 15, 16);
        assertEquals(2, procExpr.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(procExpr.formalArgs.get(0)).ident);
        assertEquals(Ident.create("b"), asIdentAsPat(procExpr.formalArgs.get(1)).ident);
        assertEquals(1, procExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc (a, b) in
                0
            end""";
        String actualFormat = procExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmt() {
        //                                      1         2
        //                            01234567890123456789012
        Parser p = new Parser("proc MyProc() in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcStmt.class, sox);
        ProcStmt procStmt = (ProcStmt) sox;
        assertSourceSpan(procStmt, 0, 22);
        assertEquals(Ident.create("MyProc"), procStmt.name());
        assertSourceSpan(procStmt.body, 17, 18);
        assertEquals(0, procStmt.formalArgs.size());
        assertEquals(1, procStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc MyProc() in
                0
            end""";
        String actualFormat = procStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmtWithArgs1() {
        //                                      1         2
        //                            012345678901234567890123
        Parser p = new Parser("proc MyProc(a) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcStmt.class, sox);
        ProcStmt procStmt = (ProcStmt) sox;
        assertSourceSpan(procStmt, 0, 23);
        assertEquals(Ident.create("MyProc"), procStmt.name());
        assertSourceSpan(procStmt.body, 18, 19);
        assertEquals(1, procStmt.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(procStmt.formalArgs.get(0)).ident);
        assertEquals(1, procStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc MyProc(a) in
                0
            end""";
        String actualFormat = procStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmtWithArgs2() {
        //                                      1         2
        //                            012345678901234567890123456
        Parser p = new Parser("proc MyProc(a, b) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ProcStmt.class, sox);
        ProcStmt procStmt = (ProcStmt) sox;
        assertSourceSpan(procStmt, 0, 26);
        assertEquals(Ident.create("MyProc"), procStmt.name());
        assertSourceSpan(procStmt.body, 21, 22);
        assertEquals(2, procStmt.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(procStmt.formalArgs.get(0)).ident);
        assertEquals(Ident.create("b"), asIdentAsPat(procStmt.formalArgs.get(1)).ident);
        assertEquals(1, procStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(procStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            proc MyProc(a, b) in
                0
            end""";
        String actualFormat = procStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

}
