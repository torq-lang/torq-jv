/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.*;

public class TestParserRecExpr {

    @Test
    public void testBraceExpected() {
        // Empty with and without dangling comma
        String source = "{]";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
        source = "{,]";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
        // 1 Feature with and without dangling comma
        source = "{'0-feat': 0]";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
        source = "{'0-feat': 0,]";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
        // 2 Features with and without dangling comma
        source = "{'0-feat': 0, '1-feat': 1]";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
        source = "{'0-feat': 0, '1-feat': 1,]";
        p = new Parser(source);
        error = assertThrows(ParserError.class, p::parse);
        assertEquals("'}' expected", error.getMessage());
    }

    @Test
    public void testDanglingComma() {
        String source = "{'0-feat': 0,}";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        String expectedFormat = "{'0-feat': 0}";
        String actualFormat = sox.toString();
        assertEquals(expectedFormat, actualFormat);
        source = "{'0-feat': 0, '1-feat': 1,}";
        p = new Parser(source);
        sox = p.parse();
        expectedFormat = "{'0-feat': 0, '1-feat': 1}";
        actualFormat = sox.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testEmpty() {
        //                            012
        Parser p = new Parser("{}");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(RecExpr.class, sox);
        RecExpr recExpr = (RecExpr) sox;
        assertSourceSpan(recExpr, 0, 2);
        assertNull(recExpr.label());
        assertEquals(0, recExpr.fields().size());
        // Test toString format
        String expectedFormat = "{}";
        String actualFormat = recExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "{}";
        actualFormat = LangFormatter.DEFAULT.format(recExpr);
        assertEquals(expectedFormat, actualFormat);
        // Test complete
        CompleteRec completeRec = recExpr.checkComplete();
        assertNotNull(completeRec);
        assertEquals(Rec.DEFAULT_LABEL, completeRec.label());
        assertEquals(0, completeRec.fieldCount());
    }

    @Test
    public void testFeatures1() {
        Str zeroFeat = Str.of("0-feat");
        //                                      1
        //                            01234567890123
        Parser p = new Parser("{'0-feat': 0}");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(RecExpr.class, sox);
        RecExpr recExpr = (RecExpr) sox;
        assertSourceSpan(recExpr, 0, 13);
        assertNull(recExpr.label());
        // Test features
        assertEquals(1, recExpr.fields().size());
        FieldExpr fieldExpr = recExpr.fields().get(0);
        assertSourceSpan(fieldExpr, 1, 12);
        assertEquals(zeroFeat, asStrAsExpr(fieldExpr.feature).str);
        assertEquals(Int32.I32_0, asIntAsExpr(fieldExpr.value).int64());
        // Test toString format
        String expectedFormat = "{'0-feat': 0}";
        String actualFormat = recExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "{'0-feat': 0}";
        actualFormat = LangFormatter.DEFAULT.format(recExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures1WithLabel() {
        Str zeroFeat = Str.of("0-feat");
        //                                      1         2
        //                            0123456789012345678901234
        Parser p = new Parser("'my-label'#{'0-feat': 0}");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(RecExpr.class, sox);
        RecExpr recExpr = (RecExpr) sox;
        assertSourceSpan(recExpr, 0, 24);
        assertEquals(Str.of("my-label"), asStrAsExpr(recExpr.label()).str);
        // Test features
        assertEquals(1, recExpr.fields().size());
        FieldExpr fieldExpr = recExpr.fields().get(0);
        assertSourceSpan(fieldExpr, 12, 23);
        assertEquals(zeroFeat, asStrAsExpr(fieldExpr.feature).str);
        assertEquals(Int32.I32_0, asIntAsExpr(fieldExpr.value).int64());
        // Test toString format
        String expectedFormat = "'my-label'#{'0-feat': 0}";
        String actualFormat = recExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "'my-label'#{'0-feat': 0}";
        actualFormat = LangFormatter.DEFAULT.format(recExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testFeatures2() {
        Str zeroFeat = Str.of("0-feat");
        Str oneFeat = Str.of("1-feat");
        //                                      1         2
        //                            012345678901234567890123456
        Parser p = new Parser("{'0-feat': 0, '1-feat': 1}");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(RecExpr.class, sox);
        RecExpr recExpr = (RecExpr) sox;
        assertSourceSpan(recExpr, 0, 26);
        assertNull(recExpr.label());
        // Test features
        assertEquals(2, recExpr.fields().size());
        FieldExpr fieldExpr = recExpr.fields().get(0);
        assertSourceSpan(fieldExpr, 1, 12);
        assertEquals(zeroFeat, asStrAsExpr(fieldExpr.feature).str);
        assertEquals(Int32.I32_0, asIntAsExpr(fieldExpr.value).int64());
        fieldExpr = recExpr.fields().get(1);
        assertSourceSpan(fieldExpr, 14, 25);
        assertEquals(oneFeat, asStrAsExpr(fieldExpr.feature).str);
        assertEquals(Int32.I32_1, asIntAsExpr(fieldExpr.value).int64());
        // Test toString format
        String expectedFormat = "{'0-feat': 0, '1-feat': 1}";
        String actualFormat = recExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "{'0-feat': 0, '1-feat': 1}";
        actualFormat = LangFormatter.DEFAULT.format(recExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testImpliedFeatures() {
        String source = "{'0-feat': 0, 15}";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();

    }

    @Test
    public void testStaticComplete() {

        // ValueAsExpr
        Parser p = new Parser("0");
        StmtOrExpr sox = p.parse();
        IntAsExpr intAsExpr = CommonTools.asIntAsExpr(sox);
        Complete complete = RecExpr.checkComplete(intAsExpr);
        assertInstanceOf(Int32.class, complete);

        // An identifier that is an unknown value
        p = new Parser("a");
        sox = p.parse();
        complete = RecExpr.checkComplete(sox);
        assertNull(complete);

        // RecExpr
        p = new Parser("'my-label'#{'0-feat': 0}");
        sox = p.parse();
        RecExpr recExpr = (RecExpr) sox;
        complete = RecExpr.checkComplete(recExpr);
        assertInstanceOf(CompleteRec.class, complete);

        // RecExpr with label identifier
        p = new Parser("x#{'0-feat': 0}");
        sox = p.parse();
        recExpr = (RecExpr) sox;
        complete = RecExpr.checkComplete(recExpr);
        assertNull(complete);

        // RecExpr with feature identifier
        p = new Parser("'my-label'#{x: 0}");
        sox = p.parse();
        recExpr = (RecExpr) sox;
        complete = RecExpr.checkComplete(recExpr);
        assertNull(complete);

        // RecExpr with value identifier
        p = new Parser("'my-label'#{'0-feat': x}");
        sox = p.parse();
        recExpr = (RecExpr) sox;
        complete = RecExpr.checkComplete(recExpr);
        assertNull(complete);

        // TupleExpr
        p = new Parser("'my-label'#[0]");
        sox = p.parse();
        TupleExpr tupleExpr = (TupleExpr) sox;
        complete = RecExpr.checkComplete(tupleExpr);
        assertInstanceOf(CompleteTuple.class, complete);

        // TupleExpr with label identifier
        p = new Parser("x#[0]");
        sox = p.parse();
        tupleExpr = (TupleExpr) sox;
        complete = RecExpr.checkComplete(tupleExpr);
        assertNull(complete);

        // TupleExpr with value identifier
        p = new Parser("'my-label'#[x]");
        sox = p.parse();
        tupleExpr = (TupleExpr) sox;
        complete = RecExpr.checkComplete(tupleExpr);
        assertNull(complete);
    }

}
