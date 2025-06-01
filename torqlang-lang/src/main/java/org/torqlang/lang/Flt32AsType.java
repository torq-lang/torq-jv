/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Flt32;
import org.torqlang.util.SourceSpan;

public final class Flt32AsType extends AbstractLang implements Flt32Type, NumAsType {

    public final Flt32 flt32;

    public Flt32AsType(Flt32 flt32, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.flt32 = flt32;
    }

    public static Flt32AsType create(Flt32 flt32) {
        return new Flt32AsType(flt32, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitScalarAsType(this, state);
    }

    @Override
    public final Flt32 value() {
        return flt32;
    }
}
