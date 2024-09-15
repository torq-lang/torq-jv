/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.ActorSystem;
import org.torqlang.local.Address;
import org.torqlang.local.AffinityExecutor;

import java.util.concurrent.Executor;

public class BenchIdiomaticAsyncAwait {

    private void perform() {
        int concurrencyLevel = Runtime.getRuntime().availableProcessors();
        Executor executor = new AffinityExecutor(Runtime.getRuntime().availableProcessors());
        ActorSystem system = ActorSystem.builder()
            .setName("NorthwindDb")
            .setExecutor(executor)
            .build();
        NorthwindDb db = new NorthwindDb(Address.create("northwind_db"), system, concurrencyLevel, 0);

    }

}
