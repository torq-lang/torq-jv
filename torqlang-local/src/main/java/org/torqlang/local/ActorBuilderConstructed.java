/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteOrIdent;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Instr;
import org.torqlang.lang.ActorExpr;
import org.torqlang.lang.ActorStmt;

import java.util.List;

public interface ActorBuilderConstructed {
    ActorExpr actorExpr();

    Ident actorIdent();

    Rec actorRec();

    ActorStmt actorStmt();

    Address address();

    List<? extends CompleteOrIdent> args();

    ActorBuilderConfigured configure() throws Exception;

    ActorBuilderConfigured configure(List<? extends CompleteOrIdent> args) throws Exception;

    Instr createActorRecInstr();

    ActorBuilderConstructed setArgs(List<? extends CompleteOrIdent> args);

    String source();

    ActorBuilderSpawned spawn() throws Exception;
}
