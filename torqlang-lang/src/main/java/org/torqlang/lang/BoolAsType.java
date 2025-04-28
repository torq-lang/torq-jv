/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Bool;
import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public final class BoolAsType extends AbstractLang implements BoolType, ValueAsType, LabelType {

    public final Bool bool;

    public BoolAsType(Bool bool, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.bool = bool;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitBoolAsType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return BoolType.IDENT;
    }

    @Override
    public final Bool typeValue() {
        return bool;
    }

}
