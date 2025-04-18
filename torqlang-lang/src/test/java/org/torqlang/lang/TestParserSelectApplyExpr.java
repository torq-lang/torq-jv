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

public class TestParserSelectApplyExpr {

    @Test
    public void testApply() {
        //                            01234
        Parser p = new Parser("a(b)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ApplyLang.class, sox);
        ApplyLang applyLang = (ApplyLang) sox;
        assertSourceSpan(applyLang, 0, 4);
        // Test properties
        assertEquals(Ident.create("a"), asIdentAsExpr(applyLang.proc).ident);
        assertSourceSpan(applyLang.proc, 0, 1);
        assertSourceSpan(applyLang.args.get(0), 2, 3);
        // Test toString format
        String expectedFormat = "a(b)";
        String actualFormat = applyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a(b)";
        actualFormat = LangFormatter.DEFAULT.format(applyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testApplyApply() {
        //                            01234567
        Parser p = new Parser("a(b)(c)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ApplyLang.class, sox);
        ApplyLang applyLang = (ApplyLang) sox;
        assertSourceSpan(applyLang, 0, 7);
        // Test properties
        assertInstanceOf(ApplyLang.class, applyLang.proc);
        assertSourceSpan(applyLang.proc, 0, 4);
        assertEquals(1, applyLang.args.size());
        assertEquals(Ident.create("c"), asIdentAsExpr(applyLang.args.get(0)).ident);
        assertSourceSpan(applyLang.args.get(0), 5, 6);
        ApplyLang leftApplyLang = (ApplyLang) applyLang.proc;
        assertSourceSpan(leftApplyLang, 0, 4);
        assertEquals(1, leftApplyLang.args.size());
        assertEquals(Ident.create("a"), asIdentAsExpr(leftApplyLang.proc).ident);
        assertSourceSpan(leftApplyLang.proc, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftApplyLang.args.get(0)).ident);
        assertSourceSpan(leftApplyLang.args.get(0), 2, 3);
        // Test toString format
        String expectedFormat = "a(b)(c)";
        String actualFormat = applyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a(b)(c)";
        actualFormat = LangFormatter.DEFAULT.format(applyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testApplyIndex() {
        //                            01234567
        Parser p = new Parser("a(b)[c]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(IndexSelectExpr.class, sox);
        IndexSelectExpr indexSelectExpr = (IndexSelectExpr) sox;
        assertSourceSpan(indexSelectExpr, 0, 7);
        // Test properties
        assertInstanceOf(ApplyLang.class, indexSelectExpr.recExpr);
        ApplyLang leftApplyLang = (ApplyLang) indexSelectExpr.recExpr;
        assertSourceSpan(leftApplyLang, 0, 4);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftApplyLang.proc).ident);
        assertSourceSpan(leftApplyLang.proc, 0, 1);
        assertEquals(1, leftApplyLang.args.size());
        assertSourceSpan(leftApplyLang.args.get(0), 2, 3);
        assertEquals(Ident.create("c"), asIdentAsExpr(indexSelectExpr.featureExpr).ident);
        assertSourceSpan(indexSelectExpr.featureExpr, 5, 6);
        // Test toString format
        String expectedFormat = "a(b)[c]";
        String actualFormat = indexSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a(b)[c]";
        actualFormat = LangFormatter.DEFAULT.format(indexSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testDot() {
        //                            0123
        Parser p = new Parser("a.b");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(DotSelectExpr.class, sox);
        DotSelectExpr dotSelectExpr = (DotSelectExpr) sox;
        assertSourceSpan(dotSelectExpr, 0, 3);
        // Test properties
        assertEquals(Ident.create("a"), asIdentAsExpr(dotSelectExpr.recExpr).ident);
        assertSourceSpan(dotSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(dotSelectExpr.featureExpr).ident);
        assertSourceSpan(dotSelectExpr.featureExpr, 2, 3);
        // Test toString format
        String expectedFormat = "a.b";
        String actualFormat = dotSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a.b";
        actualFormat = LangFormatter.DEFAULT.format(dotSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testDotApply() {
        //                            0123456
        Parser p = new Parser("a.b(c)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(SelectAndApplyLang.class, sox);
        SelectAndApplyLang selectAndApplyLang = (SelectAndApplyLang) sox;
        assertSourceSpan(selectAndApplyLang, 0, 6);
        // Test properties
        assertInstanceOf(DotSelectExpr.class, selectAndApplyLang.selectExpr);
        assertEquals(1, selectAndApplyLang.args.size());
        assertEquals(Ident.create("c"), asIdentAsExpr(selectAndApplyLang.args.get(0)).ident);
        DotSelectExpr leftDotSelectExpr = (DotSelectExpr) selectAndApplyLang.selectExpr;
        assertSourceSpan(leftDotSelectExpr, 0, 3);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftDotSelectExpr.recExpr).ident);
        assertSourceSpan(leftDotSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftDotSelectExpr.featureExpr).ident);
        assertSourceSpan(leftDotSelectExpr.featureExpr, 2, 3);
        // Test toString format
        String expectedFormat = "a.b(c)";
        String actualFormat = selectAndApplyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a.b(c)";
        actualFormat = LangFormatter.DEFAULT.format(selectAndApplyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testDotDot() {
        //                            012345
        Parser p = new Parser("a.b.c");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(DotSelectExpr.class, sox);
        DotSelectExpr dotSelectExpr = (DotSelectExpr) sox;
        assertSourceSpan(dotSelectExpr, 0, 5);
        // Test properties (select is left associative)
        assertInstanceOf(DotSelectExpr.class, dotSelectExpr.recExpr);
        DotSelectExpr leftDotSelectExpr = (DotSelectExpr) dotSelectExpr.recExpr;
        assertSourceSpan(leftDotSelectExpr, 0, 3);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftDotSelectExpr.recExpr).ident);
        assertSourceSpan(leftDotSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftDotSelectExpr.featureExpr).ident);
        assertSourceSpan(leftDotSelectExpr.featureExpr, 2, 3);
        assertEquals(Ident.create("c"), asIdentAsExpr(dotSelectExpr.featureExpr).ident);
        assertSourceSpan(dotSelectExpr.featureExpr, 4, 5);
        // Test toString format
        String expectedFormat = "a.b.c";
        String actualFormat = dotSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a.b.c";
        actualFormat = LangFormatter.DEFAULT.format(dotSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testDotIndex() {
        //                            0123456
        Parser p = new Parser("a.b[c]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(IndexSelectExpr.class, sox);
        IndexSelectExpr indexSelectExpr = (IndexSelectExpr) sox;
        assertSourceSpan(indexSelectExpr, 0, 6);
        // Test properties (select is left associative)
        assertInstanceOf(DotSelectExpr.class, indexSelectExpr.recExpr);
        DotSelectExpr leftDotSelectExpr = (DotSelectExpr) indexSelectExpr.recExpr;
        assertSourceSpan(leftDotSelectExpr, 0, 3);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftDotSelectExpr.recExpr).ident);
        assertSourceSpan(leftDotSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftDotSelectExpr.featureExpr).ident);
        assertSourceSpan(leftDotSelectExpr.featureExpr, 2, 3);
        assertEquals(Ident.create("c"), asIdentAsExpr(indexSelectExpr.featureExpr).ident);
        assertSourceSpan(indexSelectExpr.featureExpr, 4, 5);
        // Test toString format
        String expectedFormat = "a.b[c]";
        String actualFormat = indexSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a.b[c]";
        actualFormat = LangFormatter.DEFAULT.format(indexSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testIndex() {
        //                            01234
        Parser p = new Parser("a[b]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(IndexSelectExpr.class, sox);
        IndexSelectExpr indexSelectExpr = (IndexSelectExpr) sox;
        assertSourceSpan(indexSelectExpr, 0, 4);
        // Test properties
        assertEquals(Ident.create("a"), asIdentAsExpr(indexSelectExpr.recExpr).ident);
        assertSourceSpan(indexSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(indexSelectExpr.featureExpr).ident);
        assertSourceSpan(indexSelectExpr.featureExpr, 2, 3);
        // Test toString format
        String expectedFormat = "a[b]";
        String actualFormat = indexSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a[b]";
        actualFormat = LangFormatter.DEFAULT.format(indexSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testIndexApply() {
        //                            01234567
        Parser p = new Parser("a[b](c)");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(SelectAndApplyLang.class, sox);
        SelectAndApplyLang selectAndApplyLang = (SelectAndApplyLang) sox;
        assertSourceSpan(selectAndApplyLang, 0, 7);
        // Test properties
        assertInstanceOf(IndexSelectExpr.class, selectAndApplyLang.selectExpr);
        assertEquals(1, selectAndApplyLang.args.size());
        assertEquals(Ident.create("c"), asIdentAsExpr(selectAndApplyLang.args.get(0)).ident);
        assertSourceSpan(selectAndApplyLang.args.get(0), 5, 6);
        IndexSelectExpr leftIndexSelectExpr = (IndexSelectExpr) selectAndApplyLang.selectExpr;
        assertSourceSpan(leftIndexSelectExpr, 0, 4);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftIndexSelectExpr.recExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftIndexSelectExpr.featureExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.featureExpr, 2, 3);
        // Test toString format
        String expectedFormat = "a[b](c)";
        String actualFormat = selectAndApplyLang.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a[b](c)";
        actualFormat = LangFormatter.DEFAULT.format(selectAndApplyLang);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testIndexDot() {
        //                            0123456
        Parser p = new Parser("a[b].c");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(DotSelectExpr.class, sox);
        DotSelectExpr dotSelectExpr = (DotSelectExpr) sox;
        assertSourceSpan(dotSelectExpr, 0, 6);
        // Test properties (select is left associative)
        assertInstanceOf(IndexSelectExpr.class, dotSelectExpr.recExpr);
        IndexSelectExpr leftIndexSelectExpr = (IndexSelectExpr) dotSelectExpr.recExpr;
        assertSourceSpan(leftIndexSelectExpr, 0, 4);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftIndexSelectExpr.recExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftIndexSelectExpr.featureExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.featureExpr, 2, 3);
        assertEquals(Ident.create("c"), asIdentAsExpr(dotSelectExpr.featureExpr).ident);
        assertSourceSpan(dotSelectExpr.featureExpr, 5, 6);
        // Test toString format
        String expectedFormat = "a[b].c";
        String actualFormat = dotSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a[b].c";
        actualFormat = LangFormatter.DEFAULT.format(dotSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

    @Test
    public void testIndexIndex() {
        //                            01234567
        Parser p = new Parser("a[b][c]");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(IndexSelectExpr.class, sox);
        IndexSelectExpr indexSelectExpr = (IndexSelectExpr) sox;
        assertSourceSpan(indexSelectExpr, 0, 7);
        // Test properties (select is left associative)
        assertInstanceOf(IndexSelectExpr.class, indexSelectExpr.recExpr);
        IndexSelectExpr leftIndexSelectExpr = (IndexSelectExpr) indexSelectExpr.recExpr;
        assertSourceSpan(leftIndexSelectExpr, 0, 4);
        assertEquals(Ident.create("a"), asIdentAsExpr(leftIndexSelectExpr.recExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.recExpr, 0, 1);
        assertEquals(Ident.create("b"), asIdentAsExpr(leftIndexSelectExpr.featureExpr).ident);
        assertSourceSpan(leftIndexSelectExpr.featureExpr, 2, 3);
        assertEquals(Ident.create("c"), asIdentAsExpr(indexSelectExpr.featureExpr).ident);
        assertSourceSpan(indexSelectExpr.featureExpr, 5, 6);
        // Test toString format
        String expectedFormat = "a[b][c]";
        String actualFormat = indexSelectExpr.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test indented format
        expectedFormat = "a[b][c]";
        actualFormat = LangFormatter.DEFAULT.format(indexSelectExpr);
        assertEquals(expectedFormat, actualFormat);
    }

}
