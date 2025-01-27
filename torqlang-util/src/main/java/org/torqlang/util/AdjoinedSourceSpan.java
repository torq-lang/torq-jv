/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

public final class AdjoinedSourceSpan implements SourceSpan {

    public final SourceSpan beginSpan;
    public final SourceSpan endSpan;
    public int hash = -1;

    public AdjoinedSourceSpan(SourceSpan span1, SourceSpan span2) {
        if (span1.sourceBegin() < span2.sourceBegin()) {
            beginSpan = span1;
        } else {
            beginSpan = span2;
        }
        if (span1.sourceEnd() > span2.sourceEnd()) {
            endSpan = span1;
        } else {
            endSpan = span2;
        }
    }

    @Override
    public final String source() {
        return beginSpan.source();
    }

    @Override
    public final int sourceBegin() {
        return beginSpan.sourceBegin();
    }

    @Override
    public final int sourceEnd() {
        return endSpan.sourceEnd();
    }

    @Override
    public final SourceSpan toSourceBegin() {
        return beginSpan.toSourceBegin();
    }

    @Override
    public final SourceSpan toSourceEnd() {
        return endSpan.toSourceEnd();
    }

}
