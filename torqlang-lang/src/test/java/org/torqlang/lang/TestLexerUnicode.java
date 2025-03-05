/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.torqlang.lang.CommonTools.getBoolean;

/*
 * CHAR_LITERAL: '&' (~('\\') | ESC_SEQ);
 * fragment ESC_SEQ: '\\' ([tbnrf'"`\\] | 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT);
 */
public class TestLexerUnicode {

    @Test
    public void test01() {
        Lexer lexer;

        lexer = new Lexer("&'\\uffff'");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer(".&'\\uffff'");
        assertTrue(lexer.nextToken().isOneCharSymbol('.'));
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\uffff'.");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isOneCharSymbol('.'));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\u0000'&'\\uffff'");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\u0000'")));
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\u0000'.&'\\uffff'");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\u0000'")));
        assertTrue(lexer.nextToken().isOneCharSymbol('.'));
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\uffff'ff");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isIdent("ff"));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\uffff'.ff");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isOneCharSymbol('.'));
        assertTrue(lexer.nextToken().isIdent("ff"));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("/**/&'\\uffff'ff");
        assertTrue(getBoolean(lexer.nextToken(false), (x) -> x.isComment() && x.substring().equals("/**/")));
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isIdent("ff"));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\uffff'/**/ff");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(getBoolean(lexer.nextToken(false), (x) -> x.isComment() && x.substring().equals("/**/")));
        assertTrue(lexer.nextToken().isIdent("ff"));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\uffff'ff/**/");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\uffff'")));
        assertTrue(lexer.nextToken().isIdent("ff"));
        assertTrue(getBoolean(lexer.nextToken(false), (x) -> x.isComment() && x.substring().equals("/**/")));
        assertTrue(lexer.nextToken().isEof());

        lexer = new Lexer("&'\\u0000'0");
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isChar() && x.substring().equals("&'\\u0000'")));
        assertTrue(getBoolean(lexer.nextToken(), (x) -> x.isInt() && x.substring().equals("0")));
        assertTrue(lexer.nextToken().isEof());
    }

}
