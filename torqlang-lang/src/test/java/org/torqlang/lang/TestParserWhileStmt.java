/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Bool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.*;

public class TestParserWhileStmt {

    @Test
    public void test() {
        //                                      1
        //                            01234567890123456789
        Parser p = new Parser("while true do a end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(WhileStmt.class, sox);
        WhileStmt whileStmt = (WhileStmt) sox;
        assertSourceSpan(whileStmt, 0, 19);
        assertEquals(Bool.TRUE, asBoolAsExpr(whileStmt.cond).bool);
        assertSourceSpan(whileStmt.cond, 6, 10);
        // Test format
        String expectedFormat = """
            while true do
                a
            end""";
        String actualFormat = whileStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(whileStmt.body, 14, 15);
        assertEquals(1, whileStmt.body.list.size());
        IdentAsExpr identAsExpr = asIdentAsExpr(asSingleExpr(whileStmt.body));
        assertSourceSpan(identAsExpr, 14, 15);
    }

    @Test
    public void testSeqWithBreakContinueReturn() {
        //                                      1         2         3
        //                            0123456789012345678901234567890123456789
        Parser p = new Parser("while true do break continue return end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(WhileStmt.class, sox);
        WhileStmt whileStmt = (WhileStmt) sox;
        assertSourceSpan(whileStmt, 0, 39);
        // Test format
        String expectedFormat = """
            while true do
                break
                continue
                return
            end""";
        String actualFormat = whileStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(whileStmt.body, 14, 35);
        assertEquals(3, whileStmt.body.list.size());
        assertInstanceOf(BreakStmt.class, whileStmt.body.list.get(0));
        BreakStmt breakStmt = (BreakStmt) whileStmt.body.list.get(0);
        assertSourceSpan(breakStmt, 14, 19);
        assertInstanceOf(ContinueStmt.class, whileStmt.body.list.get(1));
        ContinueStmt continueStmt = (ContinueStmt) whileStmt.body.list.get(1);
        assertSourceSpan(continueStmt, 20, 28);
        assertInstanceOf(ReturnStmt.class, whileStmt.body.list.get(2));
        ReturnStmt returnStmt = (ReturnStmt) whileStmt.body.list.get(2);
        assertSourceSpan(returnStmt, 29, 35);
    }

}
