/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface IdentAsProtocol extends Protocol {

    static IdentAsProtocol create(Ident ident, SourceSpan sourceSpan) {
        return new IdentAsProtocolImpl(ident, sourceSpan);
    }

    Ident protocolIdent();
}

class IdentAsProtocolImpl extends AbstractLang implements IdentAsProtocol {

    public final Ident protocolIdent;

    IdentAsProtocolImpl(Ident protocolIdent, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.protocolIdent = protocolIdent;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitIdentAsProtocol(this, state);
    }

    @Override
    public final Ident protocolIdent() {
        return protocolIdent;
    }

}
