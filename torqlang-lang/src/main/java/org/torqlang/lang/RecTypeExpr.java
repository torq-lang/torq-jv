/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.BinarySearchTools;
import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class RecTypeExpr extends AbstractLang implements RecType {

    public final LabelAsType label;
    public final List<FieldType> staticFields;
    public final List<FieldType> fields;

    public RecTypeExpr(LabelAsType label, List<FieldType> staticFields, List<FieldType> fields, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.staticFields = nullSafeCopyOf(FieldType.nullSafeSort(staticFields));
        this.fields = nullSafeCopyOf(FieldType.nullSafeSort(fields));
    }

    public static RecTypeExpr createWithFields(List<FieldType> fields) {
        return new RecTypeExpr(NullAsType.SINGLETON, List.of(), fields, SourceSpan.emptySourceSpan());
    }

    public static RecTypeExpr createWithLabelAndFields(LabelAsType label, List<FieldType> fields) {
        return new RecTypeExpr(label, List.of(), fields, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitRecTypeExpr(this, state);
    }

    public final Type findValue(FeatureAsType feature) {
        int index = BinarySearchTools.search(fields, f -> FeatureAsType.compare(feature, f.feature));
        if (index < 0) {
            return null;
        } else {
            return fields.get(index).value;
        }
    }

    @Override
    public final Ident typeIdent() {
        return RecType.IDENT;
    }
}
