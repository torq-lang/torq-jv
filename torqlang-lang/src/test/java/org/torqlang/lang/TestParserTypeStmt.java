/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.torqlang.util.NeedsImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserTypeStmt {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                type MyArray = Array[Int32]
                var list = new MyArray()
            end""";

        Parser p = new Parser(source);
        StmtOrExpr sox;
        try {
            sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name.name);
            assertEquals(0, typeStmt.typeParams.size());
            ApplyType applyType = (ApplyType) typeStmt.body;
            assertEquals(1, applyType.typeArgs.size());
            IdentAsExpr identAsExpr = (IdentAsExpr) applyType.typeArgs.get(0);
            assertEquals("Int32", identAsExpr.ident.name);
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test02() throws Exception {
        String source = """
            begin
                type MyArray = new Array[Int32]
                var list = new MyArray()
            end""";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        String errorText = error.formatWithSource(5, 50, 50);
        String expectedText = """
            00001 begin
            00002     type MyArray = new Array[Int32]
                                     ^__ Type expected
            00003     var list = new MyArray()
            00004 end""";
        assertEquals(expectedText, errorText);
    }

    @Disabled
    @Test
    public void test03() throws Exception {
        String source = """
            begin
                meta#{'export': true}
                type MyArray = Array[Int32]
                var list = new MyArray()
            end""";

        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            throw new NeedsImpl("Validate meta record is attached. Validate MyArray is an alias for Array[Int32]");
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Disabled
    @Test
    public void test04() throws Exception {
        String source = """
            begin
                type Customer = {
                    'name': Str,
                    'email': Str
                }
                var c::Customer
            end""";

        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            throw new NeedsImpl("Validate Customer is the expected type expression]");
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
