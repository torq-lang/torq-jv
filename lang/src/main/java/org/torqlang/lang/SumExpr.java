/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class SumExpr extends AbstractLang implements BuiltInApplyExpr {

    public final SntcOrExpr arg1;
    public final SumOper oper;
    public final SntcOrExpr arg2;

    public SumExpr(SntcOrExpr arg1, SumOper oper, SntcOrExpr arg2, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.arg1 = arg1;
        this.oper = oper;
        this.arg2 = arg2;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitSumExpr(this, state);
    }

}
