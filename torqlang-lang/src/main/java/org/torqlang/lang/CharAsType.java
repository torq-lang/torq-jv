/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Char;
import org.torqlang.util.SourceSpan;

public final class CharAsType extends AbstractLang implements CharType, NumAsType {

    public final Char charNum;

    public CharAsType(Char charNum, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.charNum = charNum;
    }

    public static CharAsType create(Char charNum) {
        return new CharAsType(charNum, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitScalarAsType(this, state);
    }

    @Override
    public final Char value() {
        return charNum;
    }
}
