/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;

import java.util.concurrent.Executor;

public interface ActorSystem {

    static ActorSystemBuilder builder() {
        return new ActorSystemBuilder();
    }

    static Executor defaultExecutor() {
        return ActorSystemDefaults.DEFAULT_EXECUTOR;
    }

    static ActorSystem defaultSystem() {
        return ActorSystemDefaults.DEFAULT_SYSTEM;
    }

    ActorRefObj actorAt(Address address);

    Logger createLogger();

    Mailbox createMailbox();

    Executor executor();

    CompleteRec moduleAt(String path);

    String name();
}
