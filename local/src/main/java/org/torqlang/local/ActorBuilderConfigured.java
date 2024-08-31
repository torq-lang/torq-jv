/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.ActorExpr;
import org.torqlang.lang.ActorSntc;

import java.util.List;

public interface ActorBuilderConfigured {
    ActorCfg actorCfg();

    ActorExpr actorExpr();

    Ident actorIdent();

    Rec actorRec();

    ActorSntc actorSntc();

    Address address();

    List<? extends CompleteOrIdent> args();

    ActorCfg config();

    Stmt createActorRecStmt();

    String source();

    ActorBuilderSpawned spawn() throws Exception;
}
