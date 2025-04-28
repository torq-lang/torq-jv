/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface IdentAsType extends Type {

    static IdentAsType create(Ident ident, SourceSpan sourceSpan) {
        return new IdentAsTypeImpl(ident, sourceSpan);
    }

    Ident typeIdent();
}

class IdentAsTypeImpl extends AbstractLang implements IdentAsType {

    public final Ident typeIdent;

    IdentAsTypeImpl(Ident typeIdent, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.typeIdent = typeIdent;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitIdentAsType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return typeIdent;
    }

}
