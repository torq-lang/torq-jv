/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Bool;
import org.torqlang.util.SourceSpan;

public final class BoolAsPat extends AbstractLang implements LiteralAsPat {

    public final Bool bool;

    public BoolAsPat(Bool bool, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.bool = bool;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitFeatureAsPat(this, state);
    }

    @Override
    public final Bool value() {
        return bool;
    }

}
