/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.concurrent.Executor;

final class ActorSystemDefaults {

    static final ActorSystem DEFAULT_SYSTEM;
    static final AffinityExecutor DEFAULT_EXECUTOR;

    static {
        final String systemName = "System";
        int concurrency = Runtime.getRuntime().availableProcessors();
        DEFAULT_EXECUTOR = new AffinityExecutor(systemName, concurrency);
        DEFAULT_SYSTEM = ActorSystem.builder()
            .setName(systemName)
            .setExecutor(DEFAULT_EXECUTOR)
            .build();
    }

    static Executor executor() {
        return DEFAULT_EXECUTOR;
    }

}
