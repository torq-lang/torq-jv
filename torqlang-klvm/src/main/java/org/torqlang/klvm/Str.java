/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.EscapeChar;
import org.torqlang.util.SourceString;

import java.util.Set;

public final class Str implements Literal {

    private static final ObjProcTable<Str> objProcTable = ObjProcTable.<Str>builder()
        .addEntry(Str.of("substring"), StrMod::objSubstring)
        .build();

    public final String value;

    private Str(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
    }

    private static char decodeChar(char encodedChar) {
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
        } else if (encodedChar == '\'') {
            return '\'';
        } else if (encodedChar == '"') {
            return '"';
        } else {
            throw new IllegalArgumentException("Invalid escape sequence: \\" + encodedChar);
        }
    }

    public static Str of(String value) {
        return new Str(value);
    }

    public static String quote(String value, char delimiter) {
        StringBuilder sb = new StringBuilder(value.length() * 2 + 2);
        quote(value, delimiter, sb);
        return sb.toString();
    }

    public static void quote(String source, char delimiter, StringBuilder sb) {
        sb.append(delimiter);
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c < ' ') {
                // These characters are less than unicode \\u0020 and are encoded as their well known escaped literal
                // \r, \n, \t, \f, \b or simply encoded as a unicode literal \uFFFF where FFFF is the value less
                // than 0020.
                EscapeChar.apply(c, sb);
            } else {
                if (c == '\\') {
                    sb.append("\\\\");
                } else if (c == delimiter) {
                    sb.append("\\");
                    sb.append(delimiter);
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append(delimiter);
    }

    public static String unquote(SourceString source, int begin, int end) {
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

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitScalar(this, state);
    }

    @Override
    public final Str add(Value addend) {
        return Str.of(addend.appendToString(this.value));
    }

    @Override
    public final String appendToString(String string) {
        return string + value;
    }

    @Override
    public final int compareValueTo(Value right) {
        if (!(right instanceof Str s)) {
            throw new IllegalArgumentException(KlvmMessageText.ARGUMENT_MUST_BE_A_STR);
        }
        return value.compareTo(s.value);
    }

    @Override
    public final boolean entails(Value operand, Set<Memo> memos) {
        return this.equals(operand);
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Str that = (Str) other;
        return value.equals(that.value);
    }

    @Override
    public final String formatAsKernelString() {
        return Str.quote(value, '\'');
    }

    @Override
    public final Bool greaterThan(Value right) {
        if (!(right instanceof Str s)) {
            throw new IllegalArgumentException(KlvmMessageText.ARGUMENT_MUST_BE_A_STR);
        }
        return Bool.of(value.compareTo(s.value) > 0);
    }

    @Override
    public final Bool greaterThanOrEqualTo(Value right) {
        if (!(right instanceof Str s)) {
            throw new IllegalArgumentException(KlvmMessageText.ARGUMENT_MUST_BE_A_STR);
        }
        return Bool.of(value.compareTo(s.value) >= 0);
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public final boolean isValidKey() {
        return true;
    }

    @Override
    public final Bool lessThan(Value right) {
        if (!(right instanceof Str s)) {
            throw new IllegalArgumentException(KlvmMessageText.ARGUMENT_MUST_BE_A_STR);
        }
        return Bool.of(value.compareTo(s.value) < 0);
    }

    @Override
    public final Bool lessThanOrEqualTo(Value right) {
        if (!(right instanceof Str s)) {
            throw new IllegalArgumentException(KlvmMessageText.ARGUMENT_MUST_BE_A_STR);
        }
        return Bool.of(value.compareTo(s.value) <= 0);
    }

    @Override
    public final Proc select(Feature feature) {
        return objProcTable.selectAndBind(this, feature);
    }

    @Override
    public final String toNativeValue() {
        return value;
    }

    @Override
    public final String toString() {
        return value;
    }

}
