/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.asIdentAsType;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserMetaTuple {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                meta#[0, 'one', false, eof, null, -1]
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
            assertInstanceOf(MetaTuple.class, typeStmt.metaStruct());
            MetaTuple metaTuple = (MetaTuple) typeStmt.metaStruct();
            assertEquals(6, metaTuple.values().size());
            MetaValue value = metaTuple.values().get(0);
            assertEquals(Int32.I32_0, ((Int64AsExpr) value).int64());
            value = metaTuple.values().get(1);
            assertEquals(Str.of("one"), ((StrAsExpr) value).str);
            value = metaTuple.values().get(2);
            assertEquals(Bool.FALSE, ((BoolAsExpr) value).bool);
            value = metaTuple.values().get(3);
            assertEquals(Eof.SINGLETON, ((EofAsExpr) value).value());
            value = metaTuple.values().get(4);
            assertEquals(Null.SINGLETON, ((NullAsExpr) value).value());
            value = metaTuple.values().get(5);
            assertEquals(Int32.of(-1), ((Int64AsExpr) value).int64());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test02() throws Exception {
        String source = """
            begin
                meta#[0, 'one', false, eof, null, --1]
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        ParserError error = assertThrows(ParserError.class, p::parse);
        String errorText = error.formatWithSource(5, 50, 50);
        String expectedText = """
            00001 begin
            00002     meta#[0, 'one', false, eof, null, --1]
                                                         ^__ Number expected
            00003     type MyArray = Array[Int32]
            00004 end""";
        assertEquals(expectedText, errorText);
    }

}
