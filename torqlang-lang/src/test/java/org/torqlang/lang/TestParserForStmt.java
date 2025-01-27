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
import static org.torqlang.lang.CommonTools.*;

public class TestParserForStmt {

    @Test
    public void test() {
        //                                      1         2
        //                            012345678901234567890123456
        Parser p = new Parser("for n in range do skip end");
        StmtOrExpr sox = p.parse();
        assertInstanceOf(ForStmt.class, sox);
        ForStmt forStmt = (ForStmt) sox;
        assertSourceSpan(forStmt, 0, 26);
        assertEquals(Ident.create("n"), asIdentAsPat(forStmt.pat).ident);
        assertSourceSpan(forStmt.pat, 4, 5);
        assertEquals(Ident.create("range"), asIdentAsExpr(forStmt.iter).ident);
        assertSourceSpan(forStmt.iter, 9, 14);
        // Test body
        assertEquals(1, forStmt.body.list.size());
        assertSourceSpan(forStmt.body, 18, 22);
        assertInstanceOf(SkipStmt.class, forStmt.body.list.get(0));
        // Test format
        String expectedFormat = """
            for n in range do
                skip
            end""";
        String actualFormat = forStmt.toString();
        assertEquals(expectedFormat, actualFormat);
    }

}
