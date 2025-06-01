/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private final JsonLexer lexer;
    private JsonLexerToken currentToken;

    public JsonParser(String source) {
        this.lexer = new JsonLexer(source);
    }

    public static Object parse(String source) {
        return new JsonParser(source).parse();
    }

    private void nextToken() {
        currentToken = lexer.nextToken();
    }

    public final Object parse() {
        currentToken = lexer.nextToken();
        Object answer = parseAny();
        if (currentToken.type() != JsonLexerTokenType.EOF) {
            throw new IllegalArgumentException("Unexpected token: " + currentToken);
        }
        return answer;
    }

    private Object parseAny() {
        if (currentToken.type() == JsonLexerTokenType.STRING) {
            String answer = Json.unquote(currentToken.source(), currentToken.begin(), currentToken.end());
            nextToken(); // accept string
            return answer;
        }
        if (currentToken.type() == JsonLexerTokenType.NUMBER) {
            Number answer;
            String s = currentToken.substring();
            if (s.indexOf('.') > -1) {
                answer = Double.parseDouble(s);
            } else {
                answer = Long.parseLong(s);
            }
            nextToken(); // accept number
            return answer;
        }
        if (currentToken.type() == JsonLexerTokenType.BOOLEAN) {
            Boolean answer = currentToken.firstChar() == 't';
            nextToken(); // accept boolean
            return answer;
        }
        if (currentToken.type() == JsonLexerTokenType.NULL) {
            nextToken(); // accept null
            return JsonNull.SINGLETON;
        }
        Object answer;
        if (currentToken.firstChar() == '{') {
            answer = parseObject();
        } else if (currentToken.firstChar() == '[') {
            answer = parseArray();
        } else {
            throw new IllegalArgumentException("Unexpected delimiter: " + currentToken.firstChar());
        }
        return answer;
    }

    private List<Object> parseArray() {
        List<Object> answer = new ArrayList<>();
        nextToken(); // accept '['
        while (!currentToken.isRightBracketChar()) {
            Object value = parseAny();
            answer.add(value);
            if (currentToken.isCommaChar()) {
                nextToken();
            }
        }
        nextToken(); // accept ']'
        return answer;
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> answer = new HashMap<>();
        nextToken();
        while (!currentToken.isRightBraceChar()) {
            if (currentToken.type() != JsonLexerTokenType.STRING) {
                throw new IllegalArgumentException("String expected - " + currentToken);
            }
            String key = Json.unquote(currentToken.source(), currentToken.begin(), currentToken.end());
            nextToken(); // accept key
            if (!currentToken.isColonChar()) {
                throw new IllegalArgumentException(": expected - " + currentToken);
            }
            nextToken(); // accept ':'
            Object value = parseAny();
            answer.put(key, value);
            if (currentToken.isCommaChar()) {
                nextToken();
            }
        }
        nextToken(); // accept '}'
        return answer;
    }

}
