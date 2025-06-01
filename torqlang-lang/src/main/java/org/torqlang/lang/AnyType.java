/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface AnyType extends IdentAsType {

    String NAME = "Any";
    Ident IDENT = Ident.create(NAME);

    AnyType SINGLETON = new AnyTypeImpl(SourceSpan.emptySourceSpan());

    static AnyType create(SourceSpan sourceSpan) {
        return new AnyTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return AnyType.IDENT;
    }
}

final class AnyTypeImpl extends AbstractLang implements AnyType {

    AnyTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}