/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.EscapeChar;

import java.io.StringWriter;

public final class Json {

    public static char decodeChar(char encodedChar) {
        if (encodedChar == 'r') {
            return '\r';
        } else if (encodedChar == 'n') {
            return '\n';
        } else if (encodedChar == 't') {
            return '\t';
        } else if (encodedChar == 'f') {
            return '\f';
        } else if (encodedChar == 'b') {
            return '\b';
        } else if (encodedChar == '\\') {
            return '\\';
        } else if (encodedChar == '/') {
            return '/';
        } else if (encodedChar == '"') {
            return '"';
        } else {
            throw new IllegalArgumentException("Invalid escape sequence: \\" + encodedChar);
        }
    }

    public static String format(Object jsonValue) {
        return JsonFormatter.SINGLETON.format(jsonValue);
    }

    public static Object parse(String source) {
        return new JsonParser(source).parse();
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseAndCast(String source) {
        return (T) new JsonParser(source).parse();
    }

    public static void quote(String source, StringWriter sw) {
        sw.append('"');
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            //noinspection UnnecessaryUnicodeEscape
            if (c < '\u0020') {
                EscapeChar.apply(c, sw);
            } else {
                if (c == '\\') {
                    sw.write("\\\\");
                } else if (c == '"') {
                    sw.write("\\\"");
                } else {
                    sw.write(c);
                }
            }
        }
        sw.write('"');
    }

    public static String unquote(String source, int begin, int end) {
        begin = begin + 1;
        end = end - 1;
        StringBuilder sb = new StringBuilder((end - begin) * 2);
        int i = begin;
        while (i < end) {
            char c1 = source.charAt(i);
            if (c1 == '\\') {
                char c2 = source.charAt(i + 1);
                if (c2 == 'u') {
                    int code = Integer.parseInt("" + source.charAt(i + 2) + source.charAt(i + 3) +
                        source.charAt(i + 4) + source.charAt(i + 5), 16);
                    sb.append(Character.toChars(code));
                    i += 6;
                } else {
                    c1 = decodeChar(c2);
                    sb.append(c1);
                    i += 2;
                }
            } else {
                sb.append(c1);
                i++;
            }
        }
        return sb.toString();
    }

}
