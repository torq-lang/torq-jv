/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public abstract class MatchClause extends AbstractLang {

    public final Pat pat;
    public final StmtOrExpr guard;
    public final SeqLang body;

    public MatchClause(Pat pat, StmtOrExpr guard, SeqLang body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.pat = pat;
        this.guard = guard;
        this.body = body;
    }

}
