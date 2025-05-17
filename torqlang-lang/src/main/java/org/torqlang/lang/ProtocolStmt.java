/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public class ProtocolStmt extends AbstractLang implements Stmt {

    public final IdentAsExpr name;
    public final List<TypeParam> typeParams;
    public final Protocol body;

    public ProtocolStmt(IdentAsExpr name, List<TypeParam> typeParams, Protocol body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.name = name;
        this.typeParams = nullSafeCopyOf(typeParams);
        this.body = body;
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitProtocolStmt(this, state);
    }

}
