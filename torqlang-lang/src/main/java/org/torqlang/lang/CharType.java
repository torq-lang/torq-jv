/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface CharType extends IdentAsType, NumType {

    String NAME = "Char";
    Ident IDENT = Ident.create(NAME);

    CharType SINGLETON = new CharTypeImpl(SourceSpan.emptySourceSpan());

    static CharType create(SourceSpan sourceSpan) {
        return new CharTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return CharType.IDENT;
    }
}

final class CharTypeImpl extends AbstractLang implements CharType {

    CharTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}