/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Int32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.*;

public class TestParserGroup {

    @Test
    public void test() {
        //                            0123
        Parser p = new Parser("(1)");
        StmtOrExpr x = p.parse();
        assertSourceSpan(x, 0, 3);
        assertEquals(Int32.I32_1, asInt64AsExpr(asSingleExpr(x)).int64());
        // Test toString format
        String expectedFormat = "(1)";
        String actualFormat = x.toString();
        assertEquals(expectedFormat, actualFormat);
        actualFormat = LangFormatter.DEFAULT.format(x);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testBegin() {
        //                                      1         2
        //                            0123456789012345678901234
        Parser p = new Parser("begin (1); (2); (-1) end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;

        assertSourceSpan(beginLang, 0, 24);

        assertInstanceOf(GroupExpr.class, beginLang.body.list.get(0));
        assertEquals(Int32.I32_1, asInt64AsExpr(asSingleExpr(beginLang.body.list.get(0))).int64());

        assertInstanceOf(GroupExpr.class, beginLang.body.list.get(1));
        assertEquals(Int32.I32_2, asInt64AsExpr(asSingleExpr(beginLang.body.list.get(1))).int64());

        assertInstanceOf(GroupExpr.class, beginLang.body.list.get(2));
        Int64AsExpr intAsExpr = asInt64AsExpr(asSingleExpr(beginLang.body.list.get(2)));
        assertEquals(Int32.of(-1), intAsExpr.int64());

        // Test format
        String expectedFormat = """
            begin
                (1)
                (2)
                (-1)
            end""";
        String actualFormat = sox.toString();
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testMathGroups() {
        //                                      1         2
        //                            0123456789012345678901234567
        Parser p = new Parser("begin (1 + 5) * (3 - 7) end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(BeginLang.class, sox);
        BeginLang beginLang = (BeginLang) sox;

        assertSourceSpan(beginLang, 0, 27);
        assertEquals(1, beginLang.body.list.size());
        assertInstanceOf(ProductExpr.class, asSingleExpr(beginLang.body));

        ProductExpr productExpr = (ProductExpr) beginLang.body.list.get(0);
        assertSourceSpan(productExpr, 6, 23);

        assertInstanceOf(GroupExpr.class, productExpr.arg1);
        assertSourceSpan(productExpr.arg1, 6, 13);
        assertSourceSpan(asSingleExpr(productExpr.arg1), 7, 12);
        assertInstanceOf(SumExpr.class, asSingleExpr(productExpr.arg1));

        assertInstanceOf(GroupExpr.class, productExpr.arg2);
        assertSourceSpan(productExpr.arg2, 16, 23);
        assertSourceSpan(asSingleExpr(productExpr.arg2), 17, 22);
        assertInstanceOf(SumExpr.class, asSingleExpr(productExpr.arg2));
    }

}
