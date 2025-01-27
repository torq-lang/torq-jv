/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.asIdentAsExpr;
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserThrowLang {

    @Test
    public void test() {
        //                            01234567
        Parser p = new Parser("throw x");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ThrowLang.class, sox);
        ThrowLang throwLang = (ThrowLang) sox;
        assertSourceSpan(throwLang, 0, 7);
        assertEquals(Ident.create("x"), asIdentAsExpr(throwLang.arg).ident);
        assertSourceSpan(throwLang.arg, 6, 7);
        // Test toString format
        String expectedFormat = "throw x";
        String actualFormat = throwLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "throw x";
        actualFormat = LangFormatter.DEFAULT.format(throwLang);
        assertEquals(expectedFormat, actualFormat);
    }

}
