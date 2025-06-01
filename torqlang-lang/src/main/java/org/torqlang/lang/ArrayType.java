/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface ArrayType extends StructType, IdentAsType {

    String NAME = "Array";
    Ident IDENT = Ident.create(NAME);

    ArrayType SINGLETON = new ArrayTypeImpl(SourceSpan.emptySourceSpan());

    static ArrayType create(SourceSpan sourceSpan) {
        return new ArrayTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return ArrayType.IDENT;
    }
}

final class ArrayTypeImpl extends AbstractLang implements ArrayType {

    ArrayTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}
