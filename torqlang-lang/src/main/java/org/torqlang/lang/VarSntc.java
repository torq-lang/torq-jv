/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

public final class VarSntc extends AbstractLang implements Sntc {

    public final List<VarDecl> varDecls;

    public VarSntc(List<VarDecl> varDecls, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.varDecls = varDecls;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitVarSntc(this, state);
    }

}
