/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface Flt64Type extends IdentAsType, NumType {

    String NAME = "Flt64";
    Ident IDENT = Ident.create(NAME);

    Flt64Type SINGLETON = new Flt64TypeImpl(SourceSpan.emptySourceSpan());

    static Flt64Type create(SourceSpan sourceSpan) {
        return new Flt64TypeImpl(sourceSpan);
    }
}

final class Flt64TypeImpl extends AbstractLang implements Flt64Type {

    Flt64TypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitFlt64Type(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return Flt64Type.IDENT;
    }
}
