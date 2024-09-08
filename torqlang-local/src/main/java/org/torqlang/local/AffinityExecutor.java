/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/*
 * An AffinityExecutor is a thread-per-core executor. Once an actor is assigned an executor, it is pinned to that
 * executor for life. Since Torq actors use the CPU in short bursts because they suspend or finish quickly--Torq actors
 * are non-blocking and preempted. An affinity executor groups together actors, memory, and CPU caches (L1, L2, L3).
 *
 * NOTE: Ideally, we should pin each Java thread to an underlying hardware thread. We tried using the
 *       `net.openhft.affinity` package, but our dependencies clash with its dependencies, and it does not use JPMS.
 */
public class AffinityExecutor implements Executor {

    private final AffinityThreadExecutor[] executors;

    public AffinityExecutor(int concurrency) {
        if (concurrency < 1) {
            throw new IllegalArgumentException("concurrency < 1");
        }
        executors = new AffinityThreadExecutor[concurrency];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = new AffinityThreadExecutor(i);
        }
    }

    @Override
    public void execute(Runnable runnable) throws RejectedExecutionException {
        int hash = System.identityHashCode(runnable);
        int index = hash % executors.length;
        executors[index].execute(runnable);
    }

}
