/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface Dec128Type extends IdentAsType, NumType {

    String NAME = "Dec128";
    Ident IDENT = Ident.create(NAME);

    Dec128Type SINGLETON = new Dec128TypeImpl(SourceSpan.emptySourceSpan());

    static Dec128Type create(SourceSpan sourceSpan) {
        return new Dec128TypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return Dec128Type.IDENT;
    }
}

final class Dec128TypeImpl extends AbstractLang implements Dec128Type {

    Dec128TypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}