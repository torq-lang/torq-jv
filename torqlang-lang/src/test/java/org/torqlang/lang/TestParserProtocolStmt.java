/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.lang.CommonTools.asIdentAsType;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserProtocolStmt {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                protocol HelloWorld = {
                    tell 'hello'
                }
                actor HelloWorldImpl() implements HelloWorld in
                    handle tell 'hello' in
                        skip
                    end
                end
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox;
        try {
            sox = p.parse();
            String formatted = sox.toString();
            String expectedFormat = """
                begin
                    protocol HelloWorld = {tell 'hello'}
                    actor HelloWorldImpl() implements HelloWorld in
                        handle tell 'hello' in
                            skip
                        end
                    end
                end""";
            assertEquals(expectedFormat, formatted);
            assertInstanceOf(BeginLang.class, sox);
            BeginLang begin = (BeginLang) sox;
            ProtocolStmt protocolStmt = (ProtocolStmt) begin.body.list.get(0);
            assertEquals("HelloWorld", protocolStmt.name.ident.name);
            assertEquals(0, protocolStmt.typeParams.size());
            assertInstanceOf(ProtocolStruct.class, protocolStmt.body);
            ProtocolStruct protocolStruct = (ProtocolStruct) protocolStmt.body;
            assertEquals(1, protocolStruct.handlers.size());
            assertInstanceOf(ProtocolTellHandler.class, protocolStruct.handlers.get(0));
            ProtocolTellHandler protocolTellHandler = (ProtocolTellHandler) protocolStruct.handlers.get(0);
            assertInstanceOf(StrAsPat.class, protocolTellHandler.pat);
            StrAsPat strAsPat = (StrAsPat) protocolTellHandler.pat;
            assertEquals("hello", strAsPat.str.value);
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

    @Test
    public void test02() throws Exception {
        String source = """
            begin
                protocol HelloWorld[T] = {
                    ask 'hello'#{'from': sender::T} -> Str
                }
                actor HelloWorldImpl() implements HelloWorld[Str] in
                    handle ask 'hello'#{'from': sender::Str} in
                        'Hello, ' + sender + '! Nice to meet you!'
                    end
                end
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox;
        try {
            sox = p.parse();
            String formatted = sox.toString();
            String expectedFormat = """
                begin
                    protocol HelloWorld[T] = {ask 'hello'#{'from': sender::T} -> Str}
                    actor HelloWorldImpl() implements HelloWorld[Str] in
                        handle ask 'hello'#{'from': sender::Str} in
                            'Hello, ' + sender + '! Nice to meet you!'
                        end
                    end
                end""";
            assertEquals(expectedFormat, formatted);
            assertInstanceOf(BeginLang.class, sox);
            BeginLang begin = (BeginLang) sox;
            ProtocolStmt protocolStmt = (ProtocolStmt) begin.body.list.get(0);
            assertEquals("HelloWorld", protocolStmt.name.ident.name);
            assertEquals(1, protocolStmt.typeParams.size());
            TypeParam typeParam = protocolStmt.typeParams.get(0);
            assertEquals("T", typeParam.ident.name);
            assertInstanceOf(ProtocolStruct.class, protocolStmt.body);
            ProtocolStruct protocolStruct = (ProtocolStruct) protocolStmt.body;
            assertEquals(1, protocolStruct.handlers.size());
            assertInstanceOf(ProtocolAskHandler.class, protocolStruct.handlers.get(0));
            ProtocolAskHandler protocolAskHandler = (ProtocolAskHandler) protocolStruct.handlers.get(0);
            assertInstanceOf(RecPat.class, protocolAskHandler.pat);
            RecPat recPat = (RecPat) protocolAskHandler.pat;
            assertInstanceOf(StrAsPat.class, recPat.label());
            StrAsPat labelPat = (StrAsPat) recPat.label();
            assertEquals("hello", labelPat.str.value);
            assertEquals(1, recPat.fields().size());
            FieldPat fieldPat = recPat.fields().get(0);
            assertInstanceOf(StrAsPat.class, fieldPat.feature);
            StrAsPat featurePat = (StrAsPat) fieldPat.feature;
            assertEquals("from", featurePat.str.value);
            assertInstanceOf(IdentAsPat.class, fieldPat.value);
            IdentAsPat identPat = (IdentAsPat) fieldPat.value;
            assertEquals("sender", identPat.ident.name);
            assertFalse(identPat.escaped);
            assertEquals("T", asIdentAsType(identPat.type).typeIdent().name);
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
