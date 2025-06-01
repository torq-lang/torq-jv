/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.OpaqueValue;
import org.torqlang.util.ListTools;

import java.util.List;

public class OpaqueType extends OpaqueValue {
    public final IdentAsType name;
    public final List<TypeParam> typeParams;
    public final Type body;

    public OpaqueType(IdentAsType name, List<TypeParam> typeParams, Type body) {
        this.name = name;
        this.typeParams = ListTools.nullSafeCopyOf(typeParams);
        this.body = body;
    }

    public static OpaqueType create(TypeStmt typeStmt) {
        return new OpaqueType(typeStmt.typeDecl.name, typeStmt.typeDecl.typeParams, typeStmt.body);
    }
}
