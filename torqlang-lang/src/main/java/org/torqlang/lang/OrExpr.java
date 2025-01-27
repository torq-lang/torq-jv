/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class OrExpr extends AbstractLang implements BuiltInApplyExpr {

    public final StmtOrExpr arg1;
    public final StmtOrExpr arg2;

    public OrExpr(StmtOrExpr arg1, StmtOrExpr arg2, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitOrExpr(this, state);
    }

}
