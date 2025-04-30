/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Dec128;
import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public final class Dec128AsType extends AbstractLang implements Dec128Type, NumAsType {

    public final Dec128 dec128;

    public Dec128AsType(Dec128 dec128, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.dec128 = dec128;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitDec128AsType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return Dec128Type.IDENT;
    }

    @Override
    public final Dec128 typeValue() {
        return dec128;
    }

}
