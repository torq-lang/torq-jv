/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public class PackageStmt extends AbstractLang implements Stmt {

    public static final String DEFAULT = "default";
    public static final Ident DEFAULT_IDENT = Ident.create(DEFAULT);

    public final List<IdentAsExpr> path;

    public PackageStmt(List<IdentAsExpr> path, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.path = nullSafeCopyOf(path);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitPackageStmt(this, state);
    }

    public final boolean isDefault() {
        return path.size() == 1 && path.get(0).ident.equals(DEFAULT_IDENT);
    }

}
