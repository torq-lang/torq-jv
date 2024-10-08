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

public final class RecPat extends AbstractLang implements Pat {

    private final LabelPat label;
    private final List<FieldPat> fields;
    private final boolean partialArity;

    public RecPat(LabelPat label, List<FieldPat> fields, boolean partialArity, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.fields = nullSafeCopyOf(fields);
        this.partialArity = partialArity;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitRecPat(this, state);
    }

    public final List<FieldPat> fields() {
        return fields;
    }

    public final LabelPat label() {
        return label;
    }

    public final boolean partialArity() {
        return partialArity;
    }

}
