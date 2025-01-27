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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserBeginLang {

    @Test
    public void testBegin() {
        //                                      1
        //                            012345678901
        Parser p = new Parser("begin 1 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;
        assertSourceSpan(beginLang, 0, 11);
        assertSourceSpan(beginLang.body, 6, 7);
        assertEquals(1, beginLang.body.list.size());
        // Test format
        String expectedFormat = """
            begin
                1
            end""";
        String actualFormat = beginLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test part -- seq
        assertEquals(1, beginLang.body.list.size());
        StmtOrExpr stmtOrExpr = beginLang.body.list.get(0);
        assertSourceSpan(stmtOrExpr, 6, 7);
    }

    @Test
    public void testFormat() {
        //                                      1
        //                            0123456789012345
        Parser p = new Parser("begin 1 2 3 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;
        assertSourceSpan(beginLang, 0, 15);
        assertSourceSpan(beginLang.body, 6, 11);
        String expectedFormat = """
            begin
                1
                2
                3
            end""";
        String actualFormat = LangFormatter.DEFAULT.format(beginLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testSeq() {

        Ident a = Ident.create("a");
        Ident b = Ident.create("b");

        //                                      1
        //                            012345678901234567
        Parser p = new Parser("begin a 1 b 2 end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;
        assertSourceSpan(beginLang, 0, 17);
        assertSourceSpan(beginLang.body, 6, 13);
        String expectedFormat = """
            begin
                a
                1
                b
                2
            end""";
        String actualFormat = LangFormatter.DEFAULT.format(beginLang);
        assertEquals(expectedFormat, actualFormat);
        assertEquals(4, beginLang.body.list.size());
        List<StmtOrExpr> list = beginLang.body.list;
        assertSourceSpan(list.get(0), 6, 7);
        assertEquals(a, CommonTools.asIdentAsExpr(list.get(0)).ident);
        assertSourceSpan(list.get(1), 8, 9);
        assertEquals(Int32.I32_1, CommonTools.asIntAsExpr(list.get(1)).int64());
        assertSourceSpan(list.get(2), 10, 11);
        assertEquals(b, CommonTools.asIdentAsExpr(list.get(2)).ident);
        assertSourceSpan(list.get(3), 12, 13);
        assertEquals(Int32.I32_2, CommonTools.asIntAsExpr(list.get(3)).int64());
    }

}
