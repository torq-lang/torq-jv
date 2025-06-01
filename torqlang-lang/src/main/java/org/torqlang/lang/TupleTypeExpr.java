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

public final class TupleTypeExpr extends AbstractLang implements TupleType {

    public final LabelAsType label;
    public final List<FieldType> staticFields;
    public final List<Type> values;

    public TupleTypeExpr(LabelAsType label, List<FieldType> staticFields, List<Type> values, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.staticFields = nullSafeCopyOf(FieldType.nullSafeSort(staticFields));
        this.values = nullSafeCopyOf(values);
    }

    public static TupleTypeExpr createWithValues(List<Type> values) {
        return new TupleTypeExpr(NullAsType.SINGLETON, List.of(), values, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitTupleTypeExpr(this, state);
    }
}
