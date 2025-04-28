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

public class TestParserSumExpr {

    @Test
    public void testIntPlusInt() {
        //                            012345
        Parser p = new Parser("3 + 5");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(SumExpr.class, sox);
        SumExpr sumExpr = (SumExpr) sox;
        assertSourceSpan(sumExpr, 0, 5);
        assertEquals("3 + 5", sumExpr.toString());
        assertInstanceOf(Int64AsExpr.class, sumExpr.arg1);
        assertEquals(Int32.I32_3, asInt64AsExpr(sumExpr.arg1).int64());
        assertSourceSpan(asInt64AsExpr(sumExpr.arg1), 0, 1);
        assertEquals(SumOper.ADD, sumExpr.oper);
        assertEquals(Int32.I32_5, asInt64AsExpr(sumExpr.arg2).int64());
        assertSourceSpan(asInt64AsExpr(sumExpr.arg2), 4, 5);
    }

    @Test
    public void testIntPlusNegativeInt() {
        //                            0123456
        Parser p = new Parser("3 + -5");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(SumExpr.class, sox);
        SumExpr sumExpr = (SumExpr) sox;
        assertSourceSpan(sumExpr, 0, 6);
        assertEquals("3 + -5", sumExpr.toString());
        assertInstanceOf(Int64AsExpr.class, sumExpr.arg1);
        assertEquals(Int32.I32_3, asInt64AsExpr(sumExpr.arg1).int64());
        assertSourceSpan(asInt64AsExpr(sumExpr.arg1), 0, 1);
        assertEquals(SumOper.ADD, sumExpr.oper);
        Int64AsExpr intAsExpr = asInt64AsExpr(sumExpr.arg2);
        assertEquals(Int32.of(-5), intAsExpr.int64());
        assertSourceSpan(sumExpr.arg2, 4, 6);
    }

    @Test
    public void testSumOper() {
        assertEquals(SumOper.ADD, SumOper.valueForSymbol("" + SymbolsAndKeywords.ADD_OPER_CHAR));
        assertEquals(SumOper.SUBTRACT, SumOper.valueForSymbol("" + SymbolsAndKeywords.SUBTRACT_OPER_CHAR));
    }

}
