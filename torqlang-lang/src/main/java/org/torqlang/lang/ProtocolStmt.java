/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public class ProtocolStmt extends AbstractLang implements NameDecl, Stmt {

    public final Ident name;
    public final List<ProtocolParam> protocolParams;
    public final Protocol body;

    public ProtocolStmt(Ident name, List<ProtocolParam> protocolParams, Protocol body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.name = name;
        this.protocolParams = nullSafeCopyOf(protocolParams);
        this.body = body;
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitProtocolStmt(this, state);
    }

    @Override
    public Ident name() {
        return name;
    }
}
