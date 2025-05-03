/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface SourceSpan {

    static SourceSpan adjoin(Collection<? extends SourceSpan> sourceSpans) {
        Iterator<? extends SourceSpan> iter = sourceSpans.iterator();
        SourceSpan answer = iter.next();
        while (iter.hasNext()) {
            answer = answer.adjoin(iter.next());
        }
        return answer;
    }

    static SourceSpan emptySourceSpan() {
        return EmptySourceSpan.EMPTY_SOURCE_SPAN;
    }

    private static boolean showLine(int i, int lineIndex, int showBefore, int showAfter) {
        return i == lineIndex ||
            (i < lineIndex && (lineIndex - i) <= showBefore) ||
            (i > lineIndex && (i - lineIndex) <= showAfter);
    }

    static LineAndChar toLineAndChar(SourceSpan sourceSpan, int baseLineNr, int baseCharNr) {
        int lineNr = baseLineNr;
        int charNr = baseCharNr;
        int i = 0;
        while (sourceSpan.source().containsIndex(i) && i < sourceSpan.sourceBegin()) {
            char c = sourceSpan.source().charAt(i);
            if (c == '\n') {
                lineNr++;
                charNr = baseCharNr;
            } else {
                charNr++;
            }
            i++;
        }
        return new LineAndChar(lineNr, charNr);
    }

    default SourceSpan adjoin(SourceSpan other) {
        return new AdjoinedSourceSpan(this, other);
    }

    default String formatSource(String message, int lineNrWidth, int showBefore, int showAfter) {
        String sourceAsString = source().content();
        LineAndChar location = toLineAndChar(this, 0, 0);
        StringBuilder answerBuf = new StringBuilder();
        int lineIndex = location.lineNr;
        List<String> sourceLines = StringTools.toSourceLines(sourceAsString, 1, lineNrWidth);
        boolean lineAppended = false;
        for (int i = 0; i < sourceLines.size(); i++) {
            String line = sourceLines.get(i);
            if (showLine(i, lineIndex, showBefore, showAfter)) {
                if (lineAppended) {
                    answerBuf.append('\n');
                }
                answerBuf.append(line);
                lineAppended = true;
            }
            if (i == lineIndex) {
                if (lineAppended) {
                    answerBuf.append('\n');
                }
                StringBuilder indentBuf = new StringBuilder();
                int indentLength = lineNrWidth + 1 + location.charNr;
                while (indentBuf.length() < indentLength) {
                    indentBuf.insert(0, " ");
                }
                answerBuf.append(indentBuf);
                answerBuf.append("^__ ");
                answerBuf.append(message != null ? message : "Error");
                lineAppended = true;
            }
        }
        return answerBuf.toString();
    }

    SourceString source();

    int sourceBegin();

    int sourceEnd();

    SourceSpan toSourceBegin();

    SourceSpan toSourceEnd();

}
