/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Bool;
import org.torqlang.klvm.Complete;
import org.torqlang.util.SourceSpan;

public final class BoolAsExpr extends AbstractLang implements ValueAsExpr, LabelExpr {

    public final Bool bool;

    public BoolAsExpr(Bool bool, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.bool = bool;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitBoolAsExpr(this, state);
    }

    @Override
    public final Complete value() {
        return bool;
    }

}
