/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.*;

public class TestParserActExpr {

    @Test
    public void test() {
        //                            0123456789
        Parser p = new Parser("act a end");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(ActExpr.class, sox);
        ActExpr actExpr = (ActExpr) sox;
        assertSourceSpan(actExpr, 0, 9);
        // Test format
        String expectedFormat = """
            act
                a
            end""";
        String actualFormat = actExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(actExpr.seq, 4, 5);
        assertEquals(1, actExpr.seq.list.size());
        IdentAsExpr identAsExpr = asIdentAsExpr(asSingleExpr(actExpr.seq));
        assertSourceSpan(identAsExpr, 4, 5);
    }

    @Test
    public void testSeqOf3() {
        //                                      1
        //                            01234567890123
        Parser p = new Parser("act a b c end");
        SntcOrExpr sox = p.parse();
        assertInstanceOf(ActExpr.class, sox);
        ActExpr actExpr = (ActExpr) sox;
        assertSourceSpan(actExpr, 0, 13);
        // Test format
        String expectedFormat = """
            act
                a
                b
                c
            end""";
        String actualFormat = actExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test seq
        assertSourceSpan(actExpr.seq, 4, 9);
        assertEquals(3, actExpr.seq.list.size());
        IdentAsExpr identAsExpr = asIdentAsExpr(actExpr.seq.list.get(0));
        assertSourceSpan(identAsExpr, 4, 5);
        identAsExpr = asIdentAsExpr(actExpr.seq.list.get(1));
        assertSourceSpan(identAsExpr, 6, 7);
        identAsExpr = asIdentAsExpr(actExpr.seq.list.get(2));
        assertSourceSpan(identAsExpr, 8, 9);
    }

}
