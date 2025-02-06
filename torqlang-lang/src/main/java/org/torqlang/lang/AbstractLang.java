/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public abstract class AbstractLang implements Lang {

    private final SourceSpan sourceSpan;
    private TypeScope typeScope;

    public AbstractLang(SourceSpan sourceSpan) {
        // infrType begins as a Java null
        this.sourceSpan = sourceSpan;
    }

    @Override
    public final void setTypeScope(TypeScope typeScope) {
        this.typeScope = typeScope;
    }

    @Override
    public final String source() {
        return sourceSpan.source();
    }

    @Override
    public final int sourceBegin() {
        return sourceSpan.sourceBegin();
    }

    @Override
    public final int sourceEnd() {
        return sourceSpan.sourceEnd();
    }

    @Override
    public final SourceSpan toSourceBegin() {
        return sourceSpan.toSourceBegin();
    }

    @Override
    public final SourceSpan toSourceEnd() {
        return sourceSpan.toSourceEnd();
    }

    @Override
    public final String toString() {
        return LangFormatter.DEFAULT.format(this);
    }

    @Override
    public final TypeScope typeScope() {
        return typeScope;
    }

}
