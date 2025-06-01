/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Char;
import org.torqlang.util.SourceSpan;

public final class CharAsExpr extends AbstractLang implements NumAsExpr, MetaValue {

    private final Char charNum;

    public CharAsExpr(Char charNum, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.charNum = charNum;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitScalarAsExpr(this, state);
    }

    public final Char charNum() {
        return charNum;
    }

    @Override
    public final Char value() {
        return charNum();
    }

}
