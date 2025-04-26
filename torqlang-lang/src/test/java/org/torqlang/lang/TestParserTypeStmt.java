/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Str;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.*;
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
            assertEquals("MyArray", typeStmt.name.ident.name);
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

    @Test
    public void test03() throws Exception {
        String source = """
            begin
                type Customer = {
                    'name': Str,
                    'email': Str
                }
                var c::Customer
            end""";
        String expectedFormat = """
            begin
                type Customer = {'email': Str, 'name': Str}
                var c::Customer
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String actualFormat = sox.toString();
            assertEquals(expectedFormat, actualFormat);
            TypeStmt typeStmt = getFromSeq(asBeginLang(sox).body, 0);
            assertEquals("Customer", typeStmt.name.ident.name);
            assertEquals(0, typeStmt.typeParams.size());
            assertInstanceOf(RecType.class, typeStmt.body);
            RecType recType = (RecType) typeStmt.body;
            assertNull(recType.label);
            assertEquals(2, recType.fields.size());
            assertEquals(Str.of("email"), asStrAsExpr(recType.fields.get(0).feature).value());
            assertEquals(Ident.create("Str"), asIdentAsExpr(recType.fields.get(0).value).ident);
            assertEquals(Str.of("name"), asStrAsExpr(recType.fields.get(1).feature).value());
            assertEquals(Ident.create("Str"), asIdentAsExpr(recType.fields.get(1).value).ident);
            VarStmt varStmt = getFromSeq(asBeginLang(sox).body, 1);
            assertEquals(1, varStmt.varDecls.size());
            IdentVarDecl identVarDecl = (IdentVarDecl) varStmt.varDecls.get(0);
            assertEquals("c", identVarDecl.identAsPat.ident.name);
            assertEquals("Customer", asIdentAsExpr(identVarDecl.identAsPat.type).ident.name);
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test04() throws Exception {
        String source = """
            begin
                type Path = [Str, Int32, Bool]
                var p::Path
            end""";
        String expectedFormat = """
            begin
                type Path = [Str, Int32, Bool]
                var p::Path
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String actualFormat = sox.toString();
            assertEquals(expectedFormat, actualFormat);
            TypeStmt typeStmt = getFromSeq(asBeginLang(sox).body, 0);
            assertEquals("Path", typeStmt.name.ident.name);
            assertEquals(0, typeStmt.typeParams.size());
            assertInstanceOf(TupleType.class, typeStmt.body);
            TupleType tupleType = (TupleType) typeStmt.body;
            assertNull(tupleType.label);
            assertEquals(3, tupleType.values.size());
            assertEquals(Ident.create("Str"), asIdentAsExpr(tupleType.values.get(0)).ident);
            assertEquals(Ident.create("Int32"), asIdentAsExpr(tupleType.values.get(1)).ident);
            assertEquals(Ident.create("Bool"), asIdentAsExpr(tupleType.values.get(2)).ident);
            VarStmt varStmt = getFromSeq(asBeginLang(sox).body, 1);
            assertEquals(1, varStmt.varDecls.size());
            IdentVarDecl identVarDecl = (IdentVarDecl) varStmt.varDecls.get(0);
            assertEquals("p", identVarDecl.identAsPat.ident.name);
            assertEquals("Path", asIdentAsExpr(identVarDecl.identAsPat.type).ident.name);
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
