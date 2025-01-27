/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Str;
import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class ImportStmt extends AbstractLang implements Stmt {

    public final Str qualifier;
    public final List<ImportName> names;

    public ImportStmt(Str qualifier, List<ImportName> names, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.qualifier = qualifier;
        this.names = nullSafeCopyOf(names);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitImportStmt(this, state);
    }

}
