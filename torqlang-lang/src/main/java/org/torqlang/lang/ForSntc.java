/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class ForSntc extends AbstractLang implements Sntc {

    public final Pat pat;
    public final SntcOrExpr iter;
    public final SeqLang body;

    public ForSntc(Pat pat, SntcOrExpr iter, SeqLang body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.pat = pat;
        this.iter = iter;
        this.body = body;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitForSntc(this, state);
    }

}
