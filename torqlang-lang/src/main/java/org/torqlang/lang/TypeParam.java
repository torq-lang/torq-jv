/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public final class TypeParam extends AbstractLang {
    public final Ident ident;
    public final TypeOper constraintOper;
    public final Type constraintArg;

    public TypeParam(Ident ident, TypeOper constraintOper, Type constraintArg, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.ident = ident;
        this.constraintOper = constraintOper;
        this.constraintArg = constraintArg;
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitTypeParam(this, state);
    }
}
