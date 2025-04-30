/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;
import org.torqlang.util.StringTools;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class CommonTools {

    static BeginLang asBeginLang(Object value) {
        assertInstanceOf(BeginLang.class, value);
        return (BeginLang) value;
    }

    static BoolAsExpr asBoolAsExpr(Object value) {
        assertInstanceOf(BoolAsExpr.class, value);
        return (BoolAsExpr) value;
    }

    static BoolAsPat asBoolAsPat(Object value) {
        assertInstanceOf(BoolAsPat.class, value);
        return (BoolAsPat) value;
    }

    static BoolType asBoolType(Object value) {
        assertInstanceOf(BoolType.class, value);
        return (BoolType) value;
    }

    static CharAsExpr asCharAsExpr(Object value) {
        assertInstanceOf(CharAsExpr.class, value);
        return (CharAsExpr) value;
    }

    static Dec128AsExpr asDec128AsExpr(Object value) {
        assertInstanceOf(Dec128AsExpr.class, value);
        return (Dec128AsExpr) value;
    }

    static EofAsExpr asEofAsExpr(Object value) {
        assertInstanceOf(EofAsExpr.class, value);
        return (EofAsExpr) value;
    }

    static EofAsPat asEofAsPat(Object value) {
        assertInstanceOf(EofAsPat.class, value);
        return (EofAsPat) value;
    }

    static FeatureAsPat asFeatureAsPat(Object value) {
        assertInstanceOf(FeatureAsPat.class, value);
        return (FeatureAsPat) value;
    }

    static Flt64AsExpr asFlt64AsExpr(Object value) {
        assertInstanceOf(Flt64AsExpr.class, value);
        return (Flt64AsExpr) value;
    }

    static IdentAsExpr asIdentAsExpr(Object value) {
        assertInstanceOf(IdentAsExpr.class, value);
        return (IdentAsExpr) value;
    }

    static IdentAsPat asIdentAsPat(Object value) {
        assertInstanceOf(IdentAsPat.class, value);
        return (IdentAsPat) value;
    }

    static IdentAsType asIdentAsType(Object value) {
        assertInstanceOf(IdentAsType.class, value);
        return (IdentAsType) value;
    }

    static InitVarDecl asInitVarDecl(Object value) {
        assertInstanceOf(InitVarDecl.class, value);
        return (InitVarDecl) value;
    }

    static Int64AsExpr asInt64AsExpr(Object value) {
        assertInstanceOf(Int64AsExpr.class, value);
        return (Int64AsExpr) value;
    }

    static Int64AsPat asInt64AsPat(Object value) {
        assertInstanceOf(Int64AsPat.class, value);
        return (Int64AsPat) value;
    }

    static NullAsExpr asNullAsExpr(Object value) {
        assertInstanceOf(NullAsExpr.class, value);
        return (NullAsExpr) value;
    }

    static NullAsPat asNullAsPat(Object value) {
        assertInstanceOf(NullAsPat.class, value);
        return (NullAsPat) value;
    }

    static StmtOrExpr asSingleExpr(Object value) {
        if (value instanceof GroupExpr) {
            return asSingleExprFromGroupExpr(value);
        }
        return asSingleExprFromSeqLang(value);
    }

    static StmtOrExpr asSingleExprFromGroupExpr(Object value) {
        assertInstanceOf(GroupExpr.class, value);
        GroupExpr groupExpr = (GroupExpr) value;
        if (groupExpr.expr instanceof SeqLang seqLang) {
            assertEquals(1, seqLang.list.size());
            return seqLang.list.get(0);
        } else {
            return groupExpr.expr;
        }
    }

    static StmtOrExpr asSingleExprFromSeqLang(Object value) {
        assertInstanceOf(SeqLang.class, value);
        SeqLang seqLang = (SeqLang) value;
        assertEquals(1, seqLang.list.size());
        return seqLang.list.get(0);
    }

    static StrAsExpr asStrAsExpr(Object value) {
        assertInstanceOf(StrAsExpr.class, value);
        return (StrAsExpr) value;
    }

    static StrAsPat asStrAsPat(Object value) {
        assertInstanceOf(StrAsPat.class, value);
        return (StrAsPat) value;
    }

    static StrAsType asStrAsType(Object value) {
        assertInstanceOf(StrAsType.class, value);
        return (StrAsType) value;
    }

    static Type asType(Object value) {
        assertInstanceOf(Type.class, value);
        return (Type) value;
    }

    static UnaryExpr asUnaryExpr(Object value) {
        assertInstanceOf(UnaryExpr.class, value);
        return (UnaryExpr) value;
    }

    static void assertSourceSpan(SourceSpan sourceSpan, int begin, int end) {
        assertEquals(begin, sourceSpan.sourceBegin());
        assertEquals(end, sourceSpan.sourceEnd());
    }

    static <T> Boolean getBoolean(T argument, Function<T, Boolean> function) {
        return getValue(argument, function);
    }

    @SuppressWarnings("unchecked")
    static <T> T getFromSeq(SeqLang seq, int index) {
        return (T) seq.list.get(index);
    }

    static <T, R> R getValue(T argument, Function<T, R> function) {
        return function.apply(argument);
    }

    // NOTE: This method is duplicated at test org.torqlang.local
    public static String stripCircularSpecifics(String kernelString) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < kernelString.length()) {
            char next = kernelString.charAt(i);
            if (next == '<' && i + 1 < kernelString.length() && kernelString.charAt(i + 1) == '<') {
                sb.append("<<$circular");
                i += 2;
                while (i < kernelString.length()) {
                    next = kernelString.charAt(i);
                    if (next == '>' && i + 1 < kernelString.length() && kernelString.charAt(i + 1) == '>') {
                        sb.append(">>");
                        i += 2;
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                sb.append(next);
                i++;
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    static void toStringWithEscapedControlCodes(String s, StringBuilder sb) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '\u0020') {
                sb.append("<<");
                StringTools.appendHexString(c, sb);
                sb.append(">>");
            } else {
                sb.append(c);
            }
        }
    }

}
