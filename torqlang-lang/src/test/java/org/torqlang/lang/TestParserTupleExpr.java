/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.*;

public class TestParserTupleExpr {

    @Test
    public void testBraceExpected() {
        // Empty with and without dangling comma
        String source = "[}";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
        source = "[,}";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
        // 1 Feature with and without dangling comma
        source = "[0}";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
        source = "[0,}";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
        // 2 Features with and without dangling comma
        source = "[0, 1}";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
        source = "[0, 1,}";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("']' expected", error.getMessage());
    }

    @Test
    public void testDanglingComma() {
        String source = "[0,]";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        String expectedFormat = "[0]";
        String actualFormat = sox.toString();
        assertEquals(expectedFormat, actualFormat);
        source = "[0, 1,]";
        p = new Parser(source);
        sox = p.parse();
        expectedFormat = "[0, 1]";
        actualFormat = sox.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testEmpty() {
        //                            012
        Parser p = new Parser("[]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TupleExpr.class, sox);
        TupleExpr tupleExpr = (TupleExpr) sox;
        assertSourceSpan(tupleExpr, 0, 2);
        assertNull(tupleExpr.label());
        assertEquals(0, tupleExpr.values().size());
        // Test toString format
        String expectedFormat = "[]";
        String actualFormat = tupleExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "[]";
        actualFormat = LangFormatter.DEFAULT.format(tupleExpr);
        assertEquals(expectedFormat, actualFormat);
        // Test complete
        CompleteRec completeRec = tupleExpr.checkComplete();
        assertNotNull(completeRec);
        assertEquals(Rec.DEFAULT_LABEL, completeRec.label());
        assertEquals(0, completeRec.fieldCount());
    }

    @Test
    public void testValues1() {
        //                            0123
        Parser p = new Parser("[0]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TupleExpr.class, sox);
        TupleExpr tupleExpr = (TupleExpr) sox;
        assertSourceSpan(tupleExpr, 0, 3);
        assertNull(tupleExpr.label());
        // Test features
        assertEquals(1, tupleExpr.values().size());
        StmtOrExpr valueExpr = tupleExpr.values().get(0);
        assertSourceSpan(valueExpr, 1, 2);
        assertEquals(Int32.I32_0, asInt64AsExpr(valueExpr).int64());
        // Test toString format
        String expectedFormat = "[0]";
        String actualFormat = tupleExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "[0]";
        actualFormat = LangFormatter.DEFAULT.format(tupleExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testValues1WithLabel() {
        //                                      1
        //                            012345678901234
        Parser p = new Parser("'my-label'#[0]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TupleExpr.class, sox);
        TupleExpr tupleExpr = (TupleExpr) sox;
        assertSourceSpan(tupleExpr, 0, 14);
        assertEquals(Str.of("my-label"), asStrAsExpr(tupleExpr.label()).str);
        // Test features
        assertEquals(1, tupleExpr.values().size());
        StmtOrExpr valueExpr = tupleExpr.values().get(0);
        assertSourceSpan(valueExpr, 12, 13);
        assertEquals(Int32.I32_0, asInt64AsExpr(valueExpr).int64());
        // Test toString format
        String expectedFormat = "'my-label'#[0]";
        String actualFormat = tupleExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "'my-label'#[0]";
        actualFormat = LangFormatter.DEFAULT.format(tupleExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testValues2() {
        //                            0123456
        Parser p = new Parser("[0, 1]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TupleExpr.class, sox);
        TupleExpr tupleExpr = (TupleExpr) sox;
        assertSourceSpan(tupleExpr, 0, 6);
        assertNull(tupleExpr.label());
        // Test features
        assertEquals(2, tupleExpr.values().size());
        StmtOrExpr valueExpr = tupleExpr.values().get(0);
        assertSourceSpan(valueExpr, 1, 2);
        assertEquals(Int32.I32_0, asInt64AsExpr(valueExpr).int64());
        valueExpr = tupleExpr.values().get(1);
        assertSourceSpan(valueExpr, 4, 5);
        assertEquals(Int32.I32_1, asInt64AsExpr(valueExpr).int64());
        // Test toString format
        String expectedFormat = "[0, 1]";
        String actualFormat = tupleExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "[0, 1]";
        actualFormat = LangFormatter.DEFAULT.format(tupleExpr);
        assertEquals(expectedFormat, actualFormat);
    }

}
