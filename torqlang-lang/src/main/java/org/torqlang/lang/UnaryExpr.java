/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class UnaryExpr extends AbstractLang implements BuiltInApplyExpr {

    public final UnaryOper oper;
    public final SntcOrExpr arg;

    public UnaryExpr(UnaryOper oper, SntcOrExpr arg, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.oper = oper;
        this.arg = arg;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitUnaryExpr(this, state);
    }

}