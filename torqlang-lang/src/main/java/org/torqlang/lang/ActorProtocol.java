/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface ActorProtocol extends IdentAsProtocol {
    String NAME = "Actor";
    Ident IDENT = Ident.create(NAME);

    ActorProtocol SINGLETON = new ActorProtocolImpl(SourceSpan.emptySourceSpan());

    @Override
    default Ident protocolIdent() {
        return IDENT;
    }
}

final class ActorProtocolImpl extends AbstractLang implements ActorProtocol {

    ActorProtocolImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitIdentAsProtocol(this, state);
    }
}