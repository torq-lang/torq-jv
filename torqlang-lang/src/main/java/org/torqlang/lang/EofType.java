/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface EofType extends IdentAsType, ScalarType {

    String NAME = "Eof";
    Ident IDENT = Ident.create(NAME);

    EofType SINGLETON = new EofTypeImpl(SourceSpan.emptySourceSpan());

    static EofType create(SourceSpan sourceSpan) {
        return new EofTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return EofType.IDENT;
    }
}

final class EofTypeImpl extends AbstractLang implements EofType {

    EofTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}