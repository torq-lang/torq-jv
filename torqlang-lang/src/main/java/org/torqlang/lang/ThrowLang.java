/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class ThrowLang extends AbstractLang implements StmtOrExpr {

    public final StmtOrExpr arg;

    public ThrowLang(StmtOrExpr arg, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.arg = arg;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitThrowLang(this, state);
    }

}
