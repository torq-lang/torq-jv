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
import static org.torqlang.lang.CommonTools.asIdentAsExpr;

public class TestParserIdentEncodings {

    @Test
    public void test() {

        Parser p;
        StmtOrExpr sox;
        String v;

        p = new Parser("`\\u0078`");
        sox = p.parse();
        assertInstanceOf(IdentAsExpr.class, sox);
        v = asIdentAsExpr(sox).ident.name;
        assertEquals("x", v);

        p = new Parser("`\r`");
        sox = p.parse();
        assertInstanceOf(IdentAsExpr.class, sox);
        v = asIdentAsExpr(sox).ident.name;
        assertEquals("\r", v);

        p = new Parser("` \r\t `");
        sox = p.parse();
        assertInstanceOf(IdentAsExpr.class, sox);
        v = asIdentAsExpr(sox).ident.name;
        assertEquals(" \r\t ", v);

        p = new Parser("` `");
        sox = p.parse();
        assertInstanceOf(IdentAsExpr.class, sox);
        v = asIdentAsExpr(sox).ident.name;
        assertEquals(" ", v);
    }

}
