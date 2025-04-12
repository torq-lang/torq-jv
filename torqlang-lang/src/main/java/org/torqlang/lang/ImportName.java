/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public final class ImportName extends AbstractLang {
    public final IdentAsExpr name;
    public final IdentAsExpr alias;

    public ImportName(IdentAsExpr name, IdentAsExpr alias, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.name = name;
        this.alias = alias;
    }

    public ImportName(IdentAsExpr name, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.name = name;
        this.alias = null;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitImportName(this, state);
    }
}
