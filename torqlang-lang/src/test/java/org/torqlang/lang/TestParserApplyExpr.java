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

public class TestParserApplyExpr {

    @Test
    public void test() {
        //                            0123
        Parser p = new Parser("x()");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ApplyLang.class, sox);
        ApplyLang applyLang = (ApplyLang) sox;
        assertSourceSpan(applyLang, 0, 3);
        assertEquals(Ident.create("x"), asIdentAsExpr(applyLang.proc).ident);
        assertSourceSpan(applyLang.proc, 0, 1);
        assertEquals(0, applyLang.args.size());
        // Test toString format
        String expectedFormat = "x()";
        String actualFormat = applyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "x()";
        actualFormat = LangFormatter.DEFAULT.format(applyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testArgs1() {
        //                            01234
        Parser p = new Parser("x(a)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ApplyLang.class, sox);
        ApplyLang applyLang = (ApplyLang) sox;
        assertSourceSpan(applyLang, 0, 4);
        assertEquals(Ident.create("x"), asIdentAsExpr(applyLang.proc).ident);
        assertSourceSpan(applyLang.proc, 0, 1);
        assertEquals(1, applyLang.args.size());
        assertInstanceOf(IdentAsExpr.class, applyLang.args.get(0));
        assertSourceSpan(applyLang.args.get(0), 2, 3);
        // Test toString format
        String expectedFormat = "x(a)";
        String actualFormat = applyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "x(a)";
        actualFormat = LangFormatter.DEFAULT.format(applyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testArgs2() {
        //                            01234567
        Parser p = new Parser("x(a, 3)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ApplyLang.class, sox);
        ApplyLang applyLang = (ApplyLang) sox;
        assertSourceSpan(applyLang, 0, 7);
        assertEquals(Ident.create("x"), asIdentAsExpr(applyLang.proc).ident);
        assertSourceSpan(applyLang.proc, 0, 1);
        assertEquals(2, applyLang.args.size());
        assertInstanceOf(IdentAsExpr.class, applyLang.args.get(0));
        assertSourceSpan(applyLang.args.get(0), 2, 3);
        assertInstanceOf(IntAsExpr.class, applyLang.args.get(1));
        assertSourceSpan(applyLang.args.get(1), 5, 6);
        // Test toString format
        String expectedFormat = "x(a, 3)";
        String actualFormat = applyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "x(a, 3)";
        actualFormat = LangFormatter.DEFAULT.format(applyLang);
        assertEquals(expectedFormat, actualFormat);
    }

}
