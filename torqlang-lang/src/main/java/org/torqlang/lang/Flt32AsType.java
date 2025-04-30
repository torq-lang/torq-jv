/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Flt32;
import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public final class Flt32AsType extends AbstractLang implements Flt32Type, NumAsType {

    public final Flt32 flt32;

    public Flt32AsType(Flt32 flt32, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.flt32 = flt32;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitFlt32AsType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return Flt32Type.IDENT;
    }

    @Override
    public final Flt32 typeValue() {
        return flt32;
    }

}
