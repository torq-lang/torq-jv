/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Str;
import org.torqlang.util.SourceSpan;

public final class StrAsType extends AbstractLang implements StrType, LabelAsType {

    public final Str str;

    public StrAsType(Str str, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.str = str;
    }

    public static StrAsType create(Str str) {
        return new StrAsType(str, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitScalarAsType(this, state);
    }

    @Override
    public final Str value() {
        return str;
    }

}
