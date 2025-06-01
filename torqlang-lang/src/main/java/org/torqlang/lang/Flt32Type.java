/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface Flt32Type extends IdentAsType, NumType {

    String NAME = "Flt32";
    Ident IDENT = Ident.create(NAME);

    Flt32Type SINGLETON = new Flt32TypeImpl(SourceSpan.emptySourceSpan());

    static Flt32Type create(SourceSpan sourceSpan) {
        return new Flt32TypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return Flt32Type.IDENT;
    }
}

final class Flt32TypeImpl extends AbstractLang implements Flt32Type {

    Flt32TypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}
