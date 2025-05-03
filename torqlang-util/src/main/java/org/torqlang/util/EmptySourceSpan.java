/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

public final class EmptySourceSpan implements SourceSpan {

    static final EmptySourceSpan EMPTY_SOURCE_SPAN = new EmptySourceSpan();

    private EmptySourceSpan() {
    }

    @Override
    public final SourceString source() {
        return SourceString.EMPTY_SOURCE_STRING;
    }

    @Override
    public final int sourceBegin() {
        return 0;
    }

    @Override
    public final int sourceEnd() {
        return 0;
    }

    @Override
    public final SourceSpan toSourceBegin() {
        return this;
    }

    @Override
    public final SourceSpan toSourceEnd() {
        return this;
    }

}
