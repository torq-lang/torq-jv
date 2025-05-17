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

public final class FieldType extends AbstractLang {

    public final FeatureType feature;
    public final Type value;

    public FieldType(FeatureType feature, Type value, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.feature = feature;
        this.value = value;
    }

    static List<FieldType> nullSafeSort(List<FieldType> fields) {
        if (fields == null) {
            return List.of();
        }
        ArrayList<FieldType> answer = new ArrayList<>(fields);
        answer.sort((a, b) -> {
            ScalarAsType at = (ScalarAsType) a.feature;
            ScalarAsType bt = (ScalarAsType) b.feature;
            return FeatureComparator.SINGLETON.compare((Feature) at.typeValue(), (Feature) bt.typeValue());
        });
        return answer;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitFieldType(this, state);
    }

}
