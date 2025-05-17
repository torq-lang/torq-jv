/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.concurrent.Executor;

final class ActorSystemDefaults {

    private final AffinityExecutor executor;
    private final ActorSystem system;

    private ActorSystemDefaults() {
        final String systemName = "System";
        int concurrency = Runtime.getRuntime().availableProcessors();
        executor = new AffinityExecutor(systemName, concurrency);
        system = ActorSystem.builder()
            .setName(systemName)
            .setExecutor(executor)
            .build();
    }

    static Executor executor() {
        return LazySingleton.SINGLETON.executor;
    }

    static ActorSystem system() {
        return LazySingleton.SINGLETON.system;
    }

    private static final class LazySingleton {
        private static final ActorSystemDefaults SINGLETON = new ActorSystemDefaults();
    }

}
