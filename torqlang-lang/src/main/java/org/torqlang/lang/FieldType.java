/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.FeatureComparator;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

public final class FieldType extends AbstractLang {

    public final FeatureAsType feature;
    public final Type value;

    public FieldType(FeatureAsType feature, Type value, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.feature = feature;
        this.value = value;
    }

    public static FieldType create(FeatureAsType feature, Type value) {
        return new FieldType(feature, value, SourceSpan.emptySourceSpan());
    }

    public static int compareFeatures(FieldType a, FieldType b) {
        return FeatureComparator.SINGLETON.compare(a.feature.value(), b.feature.value());
    }

    public static List<FieldType> nullSafeSort(List<FieldType> fields) {
        if (fields == null) {
            return List.of();
        }
        ArrayList<FieldType> answer = new ArrayList<>(fields);
        answer.sort(FieldType::compareFeatures);
        return answer;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitFieldType(this, state);
    }

}
