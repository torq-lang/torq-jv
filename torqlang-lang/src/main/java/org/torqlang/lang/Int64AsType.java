/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Int64;
import org.torqlang.util.SourceSpan;

public final class Int64AsType extends AbstractLang implements Int64Type, NumAsType {

    private String intText;
    private Int64 int64;

    public Int64AsType(Int64 int64, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.int64 = int64;
    }

    public Int64AsType(String intText, SourceSpan sourceSpan) {
        super(sourceSpan);
        // We must hold intermediate integers as strings during parsing. We can't hold the absolute value
        // of `Long.MIN_VALUE`, it's too large.
        this.intText = intText;
    }

    public static Int64AsType create(Int64 int64) {
        return new Int64AsType(int64, SourceSpan.emptySourceSpan());
    }

    public static Int64AsType create(String intText) {
        return new Int64AsType(intText, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitInt64AsType(this, state);
    }

    public final String intText() {
        return intText;
    }

    @Override
    public final Ident typeIdent() {
        return Int64Type.IDENT;
    }

    @Override
    public final Int64 typeValue() {
        if (intText != null) {
            int64 = NumAsExpr.parseAsInt32OrInt64(intText);
            intText = null;
        }
        return int64;
    }

}
