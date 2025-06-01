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

public class OpaqueProtocol extends OpaqueValue {
    public final IdentAsExpr name;
    public final List<TypeParam> typeParams;
    public final Protocol body;

    public OpaqueProtocol(IdentAsExpr name, List<TypeParam> typeParams, Protocol body) {
        this.name = name;
        this.typeParams = ListTools.nullSafeCopyOf(typeParams);
        this.body = body;
    }

    public static OpaqueProtocol create(ProtocolStmt protocolStmt) {
        return new OpaqueProtocol(protocolStmt.name, protocolStmt.typeParams, protocolStmt.body);
    }
}
