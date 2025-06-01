/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface NullType extends IdentAsType, ScalarType {

    String NAME = "Null";
    Ident IDENT = Ident.create(NAME);

    NullType SINGLETON = new NullTypeImpl(SourceSpan.emptySourceSpan());

    static NullType create(SourceSpan sourceSpan) {
        return new NullTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return NullType.IDENT;
    }
}

final class NullTypeImpl extends AbstractLang implements NullType {

    NullTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}