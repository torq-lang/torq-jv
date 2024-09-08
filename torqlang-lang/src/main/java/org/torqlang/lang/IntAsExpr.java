/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Int64;
import org.torqlang.util.SourceSpan;

public final class IntAsExpr extends AbstractLang implements NumAsExpr, FeatureExpr {

    private String intText;
    private Int64 int64;

    public IntAsExpr(Int64 int64, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.int64 = int64;
    }

    public IntAsExpr(String intText, SourceSpan sourceSpan) {
        super(sourceSpan);
        // We must hold intermediate integers as strings during parsing. We can't hold the absolute value
        // of `Long.MIN_VALUE`, it's too large.
        this.intText = intText;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitIntAsExpr(this, state);
    }

    public final Int64 int64() {
        if (intText != null) {
            int64 = NumAsExpr.parseAsInt32OrInt64(intText);
            intText = null;
        }
        return int64;
    }

    public final String intText() {
        return intText;
    }

    @Override
    public final Int64 value() {
        return int64();
    }

}