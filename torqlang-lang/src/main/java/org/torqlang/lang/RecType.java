/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Feature;
import org.torqlang.klvm.FeatureComparator;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class RecType extends AbstractLang implements Type {

    public final LabelType label;
    public final List<FieldType> fields;

    public RecType(LabelType label, List<FieldType> fields, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.fields = nullSafeCopyOf(sort(fields));
    }

    private static List<FieldType> sort(List<FieldType> fields) {
        ArrayList<FieldType> answer = new ArrayList<>(fields);
        answer.sort((a, b) -> {
            ValueAsExpr ax = (ValueAsExpr) a.feature;
            ValueAsExpr bx = (ValueAsExpr) b.feature;
            return FeatureComparator.SINGLETON.compare((Feature) ax.value(), (Feature) bx.value());
        });
        return answer;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitRecType(this, state);
    }

}
