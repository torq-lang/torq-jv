/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.asIdentAsExpr;
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserUnifyStmt {

    @Test
    public void test() {
        //                            012345
        Parser p = new Parser("a = b");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(UnifyStmt.class, sox);
        UnifyStmt unifyStmt = (UnifyStmt) sox;
        assertSourceSpan(unifyStmt, 0, 5);
        // Test properties
        assertEquals(Ident.create("a"), asIdentAsExpr(unifyStmt.leftSide).ident);
        assertSourceSpan(unifyStmt.leftSide, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(unifyStmt.rightSide).ident);
        assertSourceSpan(unifyStmt.rightSide, 4, 5);
        // Test toString format
        String expectedFormat = "a = b";
        String actualFormat = unifyStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a = b";
        actualFormat = LangFormatter.DEFAULT.format(unifyStmt);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testMultiline() {
        //                                      1
        //                            012345678901234567
        Parser p = new Parser("a = act b c d end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(UnifyStmt.class, sox);
        UnifyStmt unifyStmt = (UnifyStmt) sox;
        assertSourceSpan(unifyStmt, 0, 17);
        // Test properties
        assertEquals(Ident.create("a"), asIdentAsExpr(unifyStmt.leftSide).ident);
        assertSourceSpan(unifyStmt.leftSide, 0, 1);
        assertInstanceOf(ActExpr.class, unifyStmt.rightSide);
        assertSourceSpan(unifyStmt.rightSide, 4, 17);
        // Test format
        String expectedFormat = """
            a = act
                b
                c
                d
            end""";
        String actualFormat = unifyStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

}
