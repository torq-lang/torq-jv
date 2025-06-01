/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;

import java.util.concurrent.Executor;

/*
 TODO: Can we structure systems such that child systems use their parent system to access packages not found in
       the child, recursively?
 TODO: Can we use meta tags to assign actors to actor systems?
 */
public interface ActorSystem {

    static ActorSystemBuilder builder() {
        return new ActorSystemBuilder();
    }

    static Executor defaultExecutor() {
        return ActorSystemDefaults.executor();
    }

    static ActorSystem defaultSystem() {
        return ActorSystemDefaults.system();
    }

    ActorRefObj actorAt(Address address);

    Logger createLogger();

    Mailbox createMailbox();

    Executor executor();

    String name();

    CompleteRec packageAt(String qualifier);
}
