/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Str;
import org.torqlang.util.SourceSpan;

public final class StrAsType extends AbstractLang implements StrType, ValueAsType, LabelType {

    public final Str str;

    public StrAsType(Str str, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.str = str;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitStrAsType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return StrType.IDENT;
    }

    @Override
    public final Str typeValue() {
        return str;
    }

}
