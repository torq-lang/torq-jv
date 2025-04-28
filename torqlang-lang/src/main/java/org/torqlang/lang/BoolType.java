/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface BoolType extends IdentAsType, ScalarType, LabelType {

    String NAME = "Bool";
    Ident IDENT = Ident.create(NAME);

    BoolType SINGLETON = new BoolTypeImpl(SourceSpan.emptySourceSpan());

    static BoolType create(SourceSpan sourceSpan) {
        return new BoolTypeImpl(sourceSpan);
    }

    Ident typeIdent();
}

final class BoolTypeImpl extends AbstractLang implements BoolType {

    BoolTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitBoolType(this, state);
    }

    @Override
    public final Ident typeIdent() {
        return BoolType.IDENT;
    }
}