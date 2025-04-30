/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface EofType extends IdentAsType, ScalarType, LabelType {

    String NAME = "Eof";
    Ident IDENT = Ident.create(NAME);

    EofType SINGLETON = new EofTypeImpl(SourceSpan.emptySourceSpan());

    static EofType create(SourceSpan sourceSpan) {
        return new EofTypeImpl(sourceSpan);
    }
}

final class EofTypeImpl extends AbstractLang implements EofType {

    EofTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitEofType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return EofType.IDENT;
    }
}