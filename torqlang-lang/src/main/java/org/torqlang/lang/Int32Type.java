/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface Int32Type extends IdentAsType, NumType {

    String NAME = "Int32";
    Ident IDENT = Ident.create(NAME);

    Int32Type SINGLETON = new Int32TypeImpl(SourceSpan.emptySourceSpan());

    static Int32Type create(SourceSpan sourceSpan) {
        return new Int32TypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return Int32Type.IDENT;
    }
}

final class Int32TypeImpl extends AbstractLang implements Int32Type {

    Int32TypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}
