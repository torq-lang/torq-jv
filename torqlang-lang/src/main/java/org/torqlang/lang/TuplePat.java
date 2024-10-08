/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class TuplePat extends AbstractLang implements Pat {

    private final LabelPat label;
    private final List<Pat> values;
    private final boolean partialArity;

    public TuplePat(LabelPat label, List<Pat> values, boolean partialArity, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.values = nullSafeCopyOf(values);
        this.partialArity = partialArity;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitTuplePat(this, state);
    }

    public final LabelPat label() {
        return label;
    }

    public final boolean partialArity() {
        return partialArity;
    }

    public final List<Pat> values() {
        return values;
    }

}
