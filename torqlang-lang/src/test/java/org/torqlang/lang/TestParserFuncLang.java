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

public class TestParserFuncLang {

    @Test
    public void testExpr() {
        //                                      1
        //                            01234567890123456
        Parser p = new Parser("func () in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncExpr.class, sox);
        FuncExpr funcExpr = (FuncExpr) sox;
        assertSourceSpan(funcExpr, 0, 16);
        assertSourceSpan(funcExpr.body, 11, 12);
        assertEquals(0, funcExpr.formalArgs.size());
        assertEquals(1, funcExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func () in
                0
            end""";
        String actualFormat = funcExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testExprWithReturnType() {
        //                                      1         2
        //                            01234567890123456789012345
        Parser p = new Parser("func () -> Int32 in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncExpr.class, sox);
        FuncExpr funcExpr = (FuncExpr) sox;
        assertSourceSpan(funcExpr, 0, 25);
        assertSourceSpan(funcExpr.body, 20, 21);
        assertEquals(0, funcExpr.formalArgs.size());
        Type returnType = asType(funcExpr.returnType);
        assertSourceSpan(returnType, 11, 16);
        assertEquals(1, funcExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func () -> Int32 in
                0
            end""";
        String actualFormat = funcExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testExprWithArgs1() {
        //                                      1
        //                            012345678901234567
        Parser p = new Parser("func (a) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncExpr.class, sox);
        FuncExpr funcExpr = (FuncExpr) sox;
        assertSourceSpan(funcExpr, 0, 17);
        assertSourceSpan(funcExpr.body, 12, 13);
        assertEquals(1, funcExpr.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(funcExpr.formalArgs.get(0)).ident);
        assertEquals(1, funcExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func (a) in
                0
            end""";
        String actualFormat = funcExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testExprWithArgs2() {
        //                                      1         2
        //                            012345678901234567890
        Parser p = new Parser("func (a, b) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncExpr.class, sox);
        FuncExpr funcExpr = (FuncExpr) sox;
        assertSourceSpan(funcExpr, 0, 20);
        assertSourceSpan(funcExpr.body, 15, 16);
        assertEquals(2, funcExpr.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(funcExpr.formalArgs.get(0)).ident);
        assertEquals(Ident.create("b"), asIdentAsPat(funcExpr.formalArgs.get(1)).ident);
        assertEquals(1, funcExpr.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcExpr.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func (a, b) in
                0
            end""";
        String actualFormat = funcExpr.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmt() {
        //                                      1         2
        //                            01234567890123456789012
        Parser p = new Parser("func MyFunc() in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncStmt.class, sox);
        FuncStmt funcStmt = (FuncStmt) sox;
        assertSourceSpan(funcStmt, 0, 22);
        assertEquals(Ident.create("MyFunc"), funcStmt.name());
        assertSourceSpan(funcStmt.body, 17, 18);
        assertEquals(0, funcStmt.formalArgs.size());
        assertEquals(1, funcStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func MyFunc() in
                0
            end""";
        String actualFormat = funcStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmtWithArgs1() {
        //                                      1         2
        //                            012345678901234567890123
        Parser p = new Parser("func MyFunc(a) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncStmt.class, sox);
        FuncStmt funcStmt = (FuncStmt) sox;
        assertSourceSpan(funcStmt, 0, 23);
        assertEquals(Ident.create("MyFunc"), funcStmt.name());
        assertSourceSpan(funcStmt.body, 18, 19);
        assertEquals(1, funcStmt.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(funcStmt.formalArgs.get(0)).ident);
        assertEquals(1, funcStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func MyFunc(a) in
                0
            end""";
        String actualFormat = funcStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmtWithArgs2() {
        //                                      1         2
        //                            012345678901234567890123456
        Parser p = new Parser("func MyFunc(a, b) in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncStmt.class, sox);
        FuncStmt funcStmt = (FuncStmt) sox;
        assertSourceSpan(funcStmt, 0, 26);
        assertEquals(Ident.create("MyFunc"), funcStmt.name());
        assertSourceSpan(funcStmt.body, 21, 22);
        assertEquals(2, funcStmt.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(funcStmt.formalArgs.get(0)).ident);
        assertEquals(Ident.create("b"), asIdentAsPat(funcStmt.formalArgs.get(1)).ident);
        assertEquals(1, funcStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func MyFunc(a, b) in
                0
            end""";
        String actualFormat = funcStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testStmtWithArgs2WithReturnAnno() {
        //                                      1         2         3
        //                            012345678901234567890123456789012345
        Parser p = new Parser("func MyFunc(a, b) -> Int32 in 0 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(FuncStmt.class, sox);
        FuncStmt funcStmt = (FuncStmt) sox;
        assertSourceSpan(funcStmt, 0, 35);
        assertEquals(Ident.create("MyFunc"), funcStmt.name());
        assertSourceSpan(funcStmt.body, 30, 31);
        assertEquals(2, funcStmt.formalArgs.size());
        assertEquals(Ident.create("a"), asIdentAsPat(funcStmt.formalArgs.get(0)).ident);
        assertEquals(Ident.create("b"), asIdentAsPat(funcStmt.formalArgs.get(1)).ident);
        assertEquals(1, funcStmt.body.list.size());
        assertEquals(Int32.I32_0, asIntAsExpr(funcStmt.body.list.get(0)).int64());
        // Test format
        String expectedFormat = """
            func MyFunc(a, b) -> Int32 in
                0
            end""";
        String actualFormat = funcStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

}
