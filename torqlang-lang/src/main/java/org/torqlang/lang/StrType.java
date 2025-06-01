/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface StrType extends IdentAsType, ScalarType {

    String NAME = "Str";
    Ident IDENT = Ident.create(NAME);

    StrType SINGLETON = new StrTypeImpl(SourceSpan.emptySourceSpan());

    static StrType create(SourceSpan sourceSpan) {
        return new StrTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return StrType.IDENT;
    }
}

final class StrTypeImpl extends AbstractLang implements StrType {

    StrTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}