/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class NewExpr extends AbstractLang implements Expr {

    public final ApplyType typeApply;
    public final List<StmtOrExpr> args;

    public NewExpr(ApplyType typeApply,
                   List<? extends StmtOrExpr> args,
                   SourceSpan sourceSpan) {
        super(sourceSpan);
        this.typeApply = typeApply;
        this.args = nullSafeCopyOf(args);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitNewExpr(this, state);
    }

}
