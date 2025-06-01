/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface Int64Type extends IdentAsType, NumType {

    String NAME = "Int64";
    Ident IDENT = Ident.create(NAME);

    Int64Type SINGLETON = new Int64TypeImpl(SourceSpan.emptySourceSpan());

    static Int64Type create(SourceSpan sourceSpan) {
        return new Int64TypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return Int64Type.IDENT;
    }
}

final class Int64TypeImpl extends AbstractLang implements Int64Type {

    Int64TypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}
