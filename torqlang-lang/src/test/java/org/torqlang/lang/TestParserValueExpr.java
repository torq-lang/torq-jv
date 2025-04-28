/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.*;

public class TestParserValueExpr {

    @Test
    public void test() {
        //                                      1         2         3         4         5
        //                            0123456789012345678901234567890123456789012345678901234567
        Parser p = new Parser("begin a 1 1L 1.0 1.0f 1m false true null eof &'x' 'x' end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;
        assertSourceSpan(beginLang, 0, 57);
        List<StmtOrExpr> list = beginLang.body.list;
        assertSourceSpan(list.get(0), 6, 7);
        assertEquals(Ident.create("a"), asIdentAsExpr(list.get(0)).ident);
        assertSourceSpan(list.get(1), 8, 9);
        assertEquals(Int32.I32_1, asInt64AsExpr(list.get(1)).int64());
        assertSourceSpan(list.get(2), 10, 12);
        assertEquals(Int64.I64_1, asInt64AsExpr(list.get(2)).int64());
        assertSourceSpan(list.get(3), 13, 16);
        assertEquals(Flt64.of(1.0), asFlt64AsExpr(list.get(3)).flt64());
        assertSourceSpan(list.get(4), 17, 21);
        assertEquals(Flt32.of(1.0f), asFlt64AsExpr(list.get(4)).flt64());
        assertSourceSpan(list.get(5), 22, 24);
        assertEquals(Dec128.of("1"), asDec128AsExpr(list.get(5)).dec128());
        assertSourceSpan(list.get(6), 25, 30);
        assertEquals(Bool.FALSE, asBoolAsExpr(list.get(6)).value());
        assertSourceSpan(list.get(7), 31, 35);
        assertEquals(Bool.TRUE, asBoolAsExpr(list.get(7)).value());
        assertSourceSpan(list.get(8), 36, 40);
        assertEquals(Null.SINGLETON, asNullAsExpr(list.get(8)).value());
        assertSourceSpan(list.get(9), 41, 44);
        assertEquals(Eof.SINGLETON, asEofAsExpr(list.get(9)).value());
        assertSourceSpan(list.get(10), 45, 49);
        assertEquals(Char.of('x'), asCharAsExpr(list.get(10)).value());
        assertSourceSpan(list.get(11), 50, 53);
        assertEquals(Str.of("x"), asStrAsExpr(list.get(11)).value());
        String expectedFormat = """
            begin
                a
                1
                1L
                1.0
                1.0f
                1m
                false
                true
                null
                eof
                &'x'
                'x'
            end""";
        String actualFormat = beginLang.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testWithNegatives() {
        //                                      1         2         3         4
        //                            0123456789012345678901234567890123456789012345
        Parser p = new Parser("begin a; -1; -1L; -1.0; -1.0f; -1m; -&'x' end");

        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;

        assertSourceSpan(beginLang, 0, 45);
        List<StmtOrExpr> list = beginLang.body.list;
        assertSourceSpan(list.get(0), 6, 7);
        assertEquals(Ident.create("a"), asIdentAsExpr(list.get(0)).ident);

        assertSourceSpan(list.get(1), 9, 11);
        Int64AsExpr intAsExpr = asInt64AsExpr(list.get(1));
        assertEquals(Int32.of(-1), intAsExpr.int64());

        assertSourceSpan(list.get(2), 13, 16);
        intAsExpr = asInt64AsExpr(list.get(2));
        assertEquals(Int64.of(-1L), intAsExpr.int64());

        assertSourceSpan(list.get(3), 18, 22);
        Flt64AsExpr fltAsExpr = asFlt64AsExpr(list.get(3));
        assertEquals(Flt64.of(-1.0), fltAsExpr.flt64());

        assertSourceSpan(list.get(4), 24, 29);
        fltAsExpr = asFlt64AsExpr(list.get(4));
        assertEquals(Flt32.of(-1.0f), fltAsExpr.flt64());

        assertSourceSpan(list.get(5), 31, 34);
        Dec128AsExpr decAsExpr = asDec128AsExpr(list.get(5));
        assertEquals(Dec128.of("-1"), decAsExpr.dec128());

        assertSourceSpan(list.get(6), 36, 41);
        UnaryExpr unaryExpr = asUnaryExpr(list.get(6));
        assertEquals(UnaryOper.NEGATE, unaryExpr.oper);
        assertEquals(Char.of('x'), asCharAsExpr(unaryExpr.arg).charNum());

        String expectedFormat = """
            begin
                a
                -1
                -1L
                -1.0
                -1.0f
                -1m
                -&'x'
            end""";
        String actualFormat = beginLang.toString();
        assertEquals(expectedFormat, actualFormat);
    }

}
