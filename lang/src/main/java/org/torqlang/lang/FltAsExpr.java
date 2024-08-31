/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Flt64;
import org.torqlang.util.SourceSpan;

public final class FltAsExpr extends AbstractLang implements NumAsExpr {

    private String fltText;

    private Flt64 flt64;

    public FltAsExpr(Flt64 flt64, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.flt64 = flt64;
    }

    public FltAsExpr(String fltText, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.fltText = fltText;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitFltAsExpr(this, state);
    }

    public final Flt64 flt64() {
        if (fltText != null) {
            flt64 = (Flt64) NumAsExpr.parseAsFlt32OrFlt64OrDec128(fltText);
            fltText = null;
        }
        return flt64;
    }

    public final String fltText() {
        return fltText;
    }

    @Override
    public final Flt64 value() {
        return flt64();
    }

}
