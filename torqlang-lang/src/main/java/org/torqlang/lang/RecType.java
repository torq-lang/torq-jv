/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface RecType extends StructType, IdentAsType {
    String NAME = "Rec";
    Ident IDENT = Ident.create(NAME);

    RecType SINGLETON = new RecTypeImpl(SourceSpan.emptySourceSpan());

    static RecType create(SourceSpan sourceSpan) {
        return new RecTypeImpl(sourceSpan);
    }
}

final class RecTypeImpl extends AbstractLang implements RecType {

    RecTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitRecType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return RecType.IDENT;
    }
}
