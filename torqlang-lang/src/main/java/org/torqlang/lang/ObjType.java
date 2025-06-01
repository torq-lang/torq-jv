/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

public interface ObjType extends CompositeType, IdentAsType {

    String NAME = "Obj";
    Ident IDENT = Ident.create(NAME);

    ObjType SINGLETON = new ObjTypeImpl(SourceSpan.emptySourceSpan());

    static ObjType create(SourceSpan sourceSpan) {
        return new ObjTypeImpl(sourceSpan);
    }

    @Override
    default Ident ident() {
        return ObjType.IDENT;
    }
}

final class ObjTypeImpl extends AbstractObjType {

    ObjTypeImpl(SourceSpan sourceSpan) {
        super(sourceSpan);
    }
}
