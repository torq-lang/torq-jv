/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class RelationalExpr extends AbstractLang implements BuiltInApplyExpr {

    public final StmtOrExpr arg1;
    public final RelationalOper oper;
    public final StmtOrExpr arg2;

    public RelationalExpr(StmtOrExpr arg1, RelationalOper oper, StmtOrExpr arg2, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.arg1 = arg1;
        this.oper = oper;
        this.arg2 = arg2;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitRelationalExpr(this, state);
    }

}
