/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface TupleType extends StructType, IdentAsType {
    String NAME = "Tuple";
    Ident IDENT = Ident.create(NAME);

    TupleType SINGLETON = new TupleTypeImpl(SourceSpan.emptySourceSpan());

    static TupleType create(SourceSpan sourceSpan) {
        return new TupleTypeImpl(sourceSpan);
    }
}

final class TupleTypeImpl extends AbstractLang implements TupleType {

    TupleTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitTupleType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return TupleType.IDENT;
    }
}
