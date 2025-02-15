/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class TupleExpr extends AbstractLang implements Expr {

    private final Expr label;
    private final List<StmtOrExpr> values;

    public TupleExpr(Expr label, List<StmtOrExpr> values, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.label = label;
        this.values = nullSafeCopyOf(values);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitTupleExpr(this, state);
    }

    public final CompleteTuple checkComplete() {
        CompleteTupleBuilder b = Rec.completeTupleBuilder();
        StmtOrExpr label = label();
        if (label == null) {
            b.setLabel(Rec.DEFAULT_LABEL);
        } else {
            Complete l = RecExpr.checkComplete(label());
            if (l == null) {
                return null;
            }
            b.setLabel((Literal) l);
        }
        for (StmtOrExpr v : values()) {
            Complete cv = RecExpr.checkComplete(v);
            if (cv == null) {
                return null;
            }
            b.addValue(cv);
        }
        return b.build();
    }

    public final Expr label() {
        return label;
    }

    public final List<StmtOrExpr> values() {
        return values;
    }

}
