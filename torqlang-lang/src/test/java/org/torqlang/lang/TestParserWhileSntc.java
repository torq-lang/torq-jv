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

public class TestParserWhileSntc {

    @Test
    public void test() {
        //                                      1
        //                            01234567890123456789
        Parser p = new Parser("while true do a end");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(WhileSntc.class, sox);
        WhileSntc whileSntc = (WhileSntc) sox;
        assertSourceSpan(whileSntc, 0, 19);
        assertEquals(Bool.TRUE, asBoolAsExpr(whileSntc.cond).bool);
        assertSourceSpan(whileSntc.cond, 6, 10);
        // Test format
        String expectedFormat = """
            while true do
                a
            end""";
        String actualFormat = whileSntc.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(whileSntc.body, 14, 15);
        assertEquals(1, whileSntc.body.list.size());
        IdentAsExpr identAsExpr = asIdentAsExpr(asSingleExpr(whileSntc.body));
        assertSourceSpan(identAsExpr, 14, 15);
    }

    @Test
    public void testSeqWithBreakContinueReturn() {
        //                                      1         2         3
        //                            0123456789012345678901234567890123456789
        Parser p = new Parser("while true do break continue return end");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(WhileSntc.class, sox);
        WhileSntc whileSntc = (WhileSntc) sox;
        assertSourceSpan(whileSntc, 0, 39);
        // Test format
        String expectedFormat = """
            while true do
                break
                continue
                return
            end""";
        String actualFormat = whileSntc.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(whileSntc.body, 14, 35);
        assertEquals(3, whileSntc.body.list.size());
        assertInstanceOf(BreakSntc.class, whileSntc.body.list.get(0));
        BreakSntc breakSntc = (BreakSntc) whileSntc.body.list.get(0);
        assertSourceSpan(breakSntc, 14, 19);
        assertInstanceOf(ContinueSntc.class, whileSntc.body.list.get(1));
        ContinueSntc continueSntc = (ContinueSntc) whileSntc.body.list.get(1);
        assertSourceSpan(continueSntc, 20, 28);
        assertInstanceOf(ReturnSntc.class, whileSntc.body.list.get(2));
        ReturnSntc returnSntc = (ReturnSntc) whileSntc.body.list.get(2);
        assertSourceSpan(returnSntc, 29, 35);
    }

}
