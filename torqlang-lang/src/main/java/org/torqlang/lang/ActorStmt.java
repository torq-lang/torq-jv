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

public final class ActorStmt extends ActorLang implements NameDecl, Stmt {

    public final Ident name;

    public ActorStmt(Ident name, List<Pat> formalArgs, List<StmtOrExpr> body, SourceSpan sourceSpan) {
        super(formalArgs, body, sourceSpan);
        this.name = name;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitActorStmt(this, state);
    }

    @Override
    public final Ident name() {
        return name;
    }

}
