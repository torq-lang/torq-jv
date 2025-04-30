/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface TokenType extends IdentAsType {
    String NAME = "Token";
    Ident IDENT = Ident.create(NAME);

    TokenType SINGLETON = new TokenTypeImpl(SourceSpan.emptySourceSpan());

    static TokenType create(SourceSpan sourceSpan) {
        return new TokenTypeImpl(sourceSpan);
    }
}

final class TokenTypeImpl extends AbstractLang implements TokenType {

    TokenTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitTokenType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return TokenType.IDENT;
    }
}
