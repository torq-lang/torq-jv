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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserMetaTuple {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                meta#[0, 'one', false, eof, null]
                type MyArray = Array[Int32]
            end""";
        Parser p = new Parser(source);
        try {
            StmtOrExpr sox = p.parse();
            String formatted = sox.toString();
            assertEquals(source, formatted);
            BeginLang begin = (BeginLang) sox;
            TypeStmt typeStmt = (TypeStmt) begin.body.list.get(0);
            assertEquals("MyArray", typeStmt.name.ident.name);
            assertEquals(0, typeStmt.typeParams.size());
            TypeApply typeApply = (TypeApply) typeStmt.body;
            assertEquals(1, typeApply.typeArgs.size());
            IdentAsExpr identAsExpr = (IdentAsExpr) typeApply.typeArgs.get(0);
            assertEquals("Int32", identAsExpr.ident.name);
            assertNotNull(typeStmt.metaStruct());
            assertInstanceOf(MetaTuple.class, typeStmt.metaStruct());
            MetaTuple metaTuple = (MetaTuple) typeStmt.metaStruct();
            assertEquals(5, metaTuple.values().size());
            MetaValue value = metaTuple.values().get(0);
            assertEquals(Int32.I32_0, ((IntAsExpr) value).int64());
            value = metaTuple.values().get(1);
            assertEquals(Str.of("one"), ((StrAsExpr) value).str);
            value = metaTuple.values().get(2);
            assertEquals(Bool.FALSE, ((BoolAsExpr) value).bool);
            value = metaTuple.values().get(3);
            assertEquals(Eof.SINGLETON, ((EofAsExpr) value).value());
            value = metaTuple.values().get(4);
            assertEquals(Null.SINGLETON, ((NullAsExpr) value).value());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
