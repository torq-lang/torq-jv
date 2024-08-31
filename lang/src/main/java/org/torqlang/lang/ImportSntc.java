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

public final class ImportSntc extends AbstractLang implements Sntc {

    public final Str qualifier;
    public final List<ImportName> names;

    public ImportSntc(Str qualifier, List<ImportName> names, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.qualifier = qualifier;
        this.names = nullSafeCopyOf(names);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitImportSntc(this, state);
    }

}
