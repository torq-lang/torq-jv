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

public class TestParserTryLang {

    @Test
    public void testFormat() {
        //                                      1         2         3         4         5         6
        //                            012345678901234567890123456789012345678901234567890123456789012345678
        Parser p = new Parser("try 1 2 3 catch a then 4 5 6 catch b then 7 8 9 finally 10 11 12 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TryLang.class, sox);
        TryLang tryLang = (TryLang) sox;
        assertSourceSpan(tryLang, 0, 68);
        String expectedFormat = """
            try
                1
                2
                3
            catch a then
                4
                5
                6
            catch b then
                7
                8
                9
            finally
                10
                11
                12
            end""";
        String actualFormat = LangFormatter.DEFAULT.format(tryLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testTry() {
        //                                      1         2
        //                            0123456789012345678901234
        Parser p = new Parser("try 0 catch a then 1 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TryLang.class, sox);
        TryLang tryLang = (TryLang) sox;
        assertSourceSpan(tryLang, 0, 24);
        assertSourceSpan(tryLang.body, 4, 5);
        assertEquals(1, tryLang.body.list.size());
        // Test format
        String expectedFormat = """
            try
                0
            catch a then
                1
            end""";
        String actualFormat = tryLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test part -- catchClause
        assertEquals(1, tryLang.catchClauses.size());
        CatchClause catchClause = tryLang.catchClauses.get(0);
        assertSourceSpan(catchClause, 6, 20);
        // Test part -- catchClause pat
        assertInstanceOf(IdentAsPat.class, catchClause.pat);
        assertSourceSpan(catchClause.pat, 12, 13);
        // Test part -- catchClause body
        assertEquals(1, catchClause.body.list.size());
        StmtOrExpr catchBodyExpr = catchClause.body.list.get(0);
        assertInstanceOf(IntAsExpr.class, catchBodyExpr);
        assertSourceSpan(catchBodyExpr, 19, 20);
        // Test part -- finallySeq
        assertNull(tryLang.finallySeq);
    }

    @Test
    public void testTryCatchFinally() {
        //                                      1         2         3
        //                            01234567890123456789012345678901234
        Parser p = new Parser("try 0 catch a then 1 finally 2 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TryLang.class, sox);
        TryLang tryLang = (TryLang) sox;
        assertSourceSpan(tryLang, 0, 34);
        assertSourceSpan(tryLang.body, 4, 5);
        assertEquals(1, tryLang.body.list.size());
        // Test format
        String expectedFormat = """
            try
                0
            catch a then
                1
            finally
                2
            end""";
        String actualFormat = tryLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test part -- catchClause
        assertEquals(1, tryLang.catchClauses.size());
        CatchClause catchClause = tryLang.catchClauses.get(0);
        assertSourceSpan(catchClause, 6, 20);
        // Test part -- catchClause pat
        assertInstanceOf(IdentAsPat.class, catchClause.pat);
        assertSourceSpan(catchClause.pat, 12, 13);
        // Test part -- catchClause body
        assertEquals(1, catchClause.body.list.size());
        StmtOrExpr catchBodyExpr = catchClause.body.list.get(0);
        assertInstanceOf(IntAsExpr.class, catchBodyExpr);
        assertSourceSpan(catchBodyExpr, 19, 20);
        // Test part -- finallySeq
        assertNotNull(tryLang.finallySeq);
        assertEquals(1, tryLang.finallySeq.list.size());
        StmtOrExpr finallyExpr = tryLang.finallySeq.list.get(0);
        assertInstanceOf(IntAsExpr.class, finallyExpr);
        assertSourceSpan(finallyExpr, 29, 30);
    }

    @Test
    public void testTryFinally() {
        //                                      1
        //                            01234567890123456789
        Parser p = new Parser("try 0 finally 1 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TryLang.class, sox);
        TryLang tryLang = (TryLang) sox;
        assertSourceSpan(tryLang, 0, 19);
        assertSourceSpan(tryLang.body, 4, 5);
        assertEquals(1, tryLang.body.list.size());
        // Test format
        String expectedFormat = """
            try
                0
            finally
                1
            end""";
        String actualFormat = tryLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test part -- catchClause
        assertEquals(0, tryLang.catchClauses.size());
        // Test part -- finallySeq
        assertNotNull(tryLang.finallySeq);
        assertEquals(1, tryLang.finallySeq.list.size());
        StmtOrExpr finallyExpr = tryLang.finallySeq.list.get(0);
        assertInstanceOf(IntAsExpr.class, finallyExpr);
        assertSourceSpan(finallyExpr, 14, 15);
    }

}
