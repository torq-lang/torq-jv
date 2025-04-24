/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;

import java.util.concurrent.Executor;

// TODO: How can we structure systems such that child systems inherit
//       access to modules and can override executors?
// TODO: How can we use meta tags to assign actors to actor systems?
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

    // TODO: Rename to `assemblyAt`.
    //       Packages are directories that contain modules.
    //       Modules are files that contain members.
    //       Assemblies are records that contain exported members.
    CompleteRec moduleAt(String path);

    String name();
}
