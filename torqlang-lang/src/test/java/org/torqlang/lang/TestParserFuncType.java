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
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserFuncType {

    @Test
    public void test01() {
        //                         1         2         3         4         5
        //               01234567890123456789012345678901234567890123456789012
        String source = "type addInt32s = func (a::Str, b::Int32...) -> Int64";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        assertInstanceOf(TypeStmt.class, sox);
        TypeStmt typeStmt = (TypeStmt) sox;
        assertSourceSpan(typeStmt, 0, 52);
        assertEquals("addInt32s", typeStmt.name.ident.name);
        assertSourceSpan(typeStmt.name, 5, 14);
        assertSourceSpan(typeStmt.body, 17, 52);
        assertInstanceOf(FuncType.class, typeStmt.body);
        FuncType funcType = (FuncType) typeStmt.body;
        assertEquals(0, funcType.typeParams.size());
        assertEquals(2, funcType.params.size());
        assertInstanceOf(IdentAsPat.class, funcType.params.get(0));
        IdentAsPat param0 = (IdentAsPat) funcType.params.get(0);
        assertEquals("a", param0.ident.name);
        assertSourceSpan(funcType.params.get(0), 23, 29);
        assertInstanceOf(IdentAsPat.class, funcType.params.get(1));
        IdentAsPat param1 = (IdentAsPat) funcType.params.get(1);
        assertEquals("b", param1.ident.name);
        assertSourceSpan(funcType.params.get(1), 31, 42);
        String actualFormat = typeStmt.toString();
        assertEquals(source, actualFormat);
    }

}
