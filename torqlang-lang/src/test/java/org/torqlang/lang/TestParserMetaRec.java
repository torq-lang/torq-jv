/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Bool;
import org.torqlang.klvm.Flt32;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Str;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.asIdentAsType;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserMetaRec {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                meta#{'export': true}
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name().ident().name);
            assertEquals(0, typeStmt.typeParams().size());
            TypeApply typeApply = (TypeApply) typeStmt.body;
            assertEquals(1, typeApply.typeArgs.size());
            IdentAsType identAsType = asIdentAsType(typeApply.typeArgs.get(0));
            assertEquals("Int32", identAsType.ident().name);
            assertNotNull(typeStmt.metaStruct());
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            MetaRec metaRec = (MetaRec) typeStmt.metaStruct();
            assertEquals(1, metaRec.fields().size());
            MetaField field = metaRec.fields().get(0);
            assertEquals(Str.of("export"), field.feature.value());
            assertInstanceOf(BoolAsExpr.class, field.value);
            BoolAsExpr boolAsExpr = (BoolAsExpr) field.value;
            assertEquals(Bool.TRUE, boolAsExpr.value());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test02() throws Exception {
        String source = """
            begin
                meta#{'export': true, 'count': 5}
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name().ident().name);
            assertEquals(0, typeStmt.typeParams().size());
            TypeApply typeApply = (TypeApply) typeStmt.body;
            assertEquals(1, typeApply.typeArgs.size());
            IdentAsType identAsType = asIdentAsType(typeApply.typeArgs.get(0));
            assertEquals("Int32", identAsType.ident().name);
            assertNotNull(typeStmt.metaStruct());
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            MetaRec metaRec = (MetaRec) typeStmt.metaStruct();
            assertEquals(2, metaRec.fields().size());
            MetaField field = metaRec.fields().get(0);
            assertEquals(Str.of("export"), field.feature.value());
            assertInstanceOf(BoolAsExpr.class, field.value);
            BoolAsExpr boolAsExpr = (BoolAsExpr) field.value;
            assertEquals(Bool.TRUE, boolAsExpr.value());
            field = metaRec.fields().get(1);
            assertEquals(Str.of("count"), field.feature.value());
            assertInstanceOf(Int64AsExpr.class, field.value);
            Int64AsExpr intAsExpr = (Int64AsExpr) field.value;
            assertEquals(Int32.I32_5, intAsExpr.value());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test03() throws Exception {
        String source = """
            begin
                meta#{'export': true, 'numbers': [0, 1]}
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name().ident().name);
            assertEquals(0, typeStmt.typeParams().size());
            TypeApply typeApply = (TypeApply) typeStmt.body;
            assertEquals(1, typeApply.typeArgs.size());
            IdentAsType identAsType = asIdentAsType(typeApply.typeArgs.get(0));
            assertEquals("Int32", identAsType.ident().name);
            assertNotNull(typeStmt.metaStruct());
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            MetaRec metaRec = (MetaRec) typeStmt.metaStruct();
            assertEquals(2, metaRec.fields().size());
            MetaField field = metaRec.fields().get(0);
            assertEquals(Str.of("export"), field.feature.value());
            assertInstanceOf(BoolAsExpr.class, field.value);
            BoolAsExpr boolAsExpr = (BoolAsExpr) field.value;
            assertEquals(Bool.TRUE, boolAsExpr.value());
            field = metaRec.fields().get(1);
            assertEquals(Str.of("numbers"), field.feature.value());
            assertInstanceOf(MetaTuple.class, field.value);
            MetaTuple metaTuple = (MetaTuple) field.value;
            assertEquals(2, metaTuple.values().size());
            assertEquals(Int32.I32_0, ((Int64AsExpr) metaTuple.values().get(0)).int64());
            assertEquals(Int32.I32_1, ((Int64AsExpr) metaTuple.values().get(1)).int64());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test04() throws Exception {
        String source = """
            begin
                meta#[0] 0 + meta#[1] 1
            end""";
        String expectedFormat = """
            begin
                meta#[0]
                0 + meta#[1] 1
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(expectedFormat, formatted);
            BeginLang begin = (BeginLang) sox;
            SumExpr sumExpr = (SumExpr) begin.body.list.get(0);
            assertInstanceOf(MetaTuple.class, sumExpr.metaStruct());
            assertNull(sumExpr.arg1.metaStruct());
            assertInstanceOf(MetaTuple.class, sumExpr.arg2.metaStruct());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test05() throws Exception {
        String source = """
            begin
                (meta#[0] 0) + meta#[1] 1
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            SumExpr sumExpr = (SumExpr) begin.body.list.get(0);
            assertNull(sumExpr.metaStruct());
            assertInstanceOf(GroupExpr.class, sumExpr.arg1);
            GroupExpr groupExpr = (GroupExpr) sumExpr.arg1;
            assertInstanceOf(Int64AsExpr.class, groupExpr.expr);
            Int64AsExpr intAsExpr = (Int64AsExpr) groupExpr.expr;
            assertInstanceOf(MetaTuple.class, intAsExpr.metaStruct());
            assertInstanceOf(MetaTuple.class, sumExpr.arg2.metaStruct());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test06() throws Exception {
        String source = """
            meta#{'export': true, 'count': 5}""";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        String errorText = error.formatWithSource(5, 50, 50);
        String expectedText = """
            00001 meta#{'export': true, 'count': 5}
                                                   ^__ Statement or expression expected""";
        assertEquals(expectedText, errorText);
    }

    @Test
    public void test07() throws Exception {
        String source = """
            begin
                type MyArray = Array[Int32]
                meta#{'export': true, 'count': 5}
            end""";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        String errorText = error.formatWithSource(5, 50, 50);
        String expectedText = """
            00001 begin
            00002     type MyArray = Array[Int32]
            00003     meta#{'export': true, 'count': 5}
            00004 end
                  ^__ Statement or expression expected""";
        assertEquals(expectedText, errorText);
    }

    @Test
    public void test08() {
        String source = """
            begin
                meta#{'export': true, 'count': 5}
            end""";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        String errorText = error.formatWithSource(5, 50, 50);
        String expectedText = """
            00001 begin
            00002     meta#{'export': true, 'count': 5}
            00003 end
                  ^__ Statement or expression expected""";
        assertEquals(expectedText, errorText);
    }

    @Test
    public void test09() throws Exception {
        String source = """
            begin
                meta#{'export': true, 'min-value': -5.0f}
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name().ident().name);
            assertEquals(0, typeStmt.typeParams().size());
            TypeApply typeApply = (TypeApply) typeStmt.body;
            assertEquals(1, typeApply.typeArgs.size());
            assertEquals("Int32", asIdentAsType(typeApply.typeArgs.get(0)).ident().name);
            assertNotNull(typeStmt.metaStruct());
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            MetaRec metaRec = (MetaRec) typeStmt.metaStruct();
            assertEquals(2, metaRec.fields().size());
            MetaField field = metaRec.fields().get(0);
            assertEquals(Str.of("export"), field.feature.value());
            assertInstanceOf(BoolAsExpr.class, field.value);
            BoolAsExpr boolAsExpr = (BoolAsExpr) field.value;
            assertEquals(Bool.TRUE, boolAsExpr.value());
            field = metaRec.fields().get(1);
            assertEquals(Str.of("min-value"), field.feature.value());
            assertInstanceOf(Flt64AsExpr.class, field.value);
            Flt64AsExpr fltAsExpr = (Flt64AsExpr) field.value;
            assertEquals(Flt32.of(-5.0f), fltAsExpr.value());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
