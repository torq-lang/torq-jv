/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public final class IdentAsPat extends AbstractLang implements LabelPat {

    public final Ident ident;
    public final boolean escaped;
    public final TypeAnno typeAnno;

    public IdentAsPat(Ident ident, boolean escaped, SourceSpan sourceSpan) {
        this(ident, escaped, null, sourceSpan);
    }

    public IdentAsPat(Ident ident, boolean escaped, TypeAnno typeAnno, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.ident = ident;
        this.escaped = escaped;
        this.typeAnno = typeAnno;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitIdentAsPat(this, state);
    }
}
