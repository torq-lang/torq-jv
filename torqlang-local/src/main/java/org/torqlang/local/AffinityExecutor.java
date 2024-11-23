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
 * An AffinityExecutor is a thread-per-core design containing a pool of single-thread executors. When dispatched
 * to an AffinityExecutor, a runnable is mapped to a single-thread executor using its `hashCode()`. The idea is to
 * reduce CPU cache misses by executing a runnable on the same thread each time it is dispatched. At this time, there
 * is no ability to pin a Java thread to a hardware core.
 */
public final class AffinityExecutor implements Executor {

    private final String name;
    private final AffinityThreadExecutor[] executors;

    public AffinityExecutor(String name, int concurrency) {
        this.name = name;
        if (concurrency < 1) {
            throw new IllegalArgumentException("concurrency < 1");
        }
        executors = new AffinityThreadExecutor[concurrency];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = new AffinityThreadExecutor(i);
        }
    }

    @Override
    public final void execute(Runnable runnable) throws RejectedExecutionException {
        int index = Math.abs(runnable.hashCode() % executors.length);
        executors[index].execute(runnable);
    }

    public final String name() {
        return name;
    }

    @Override
    public final String toString() {
        return "AffinityExecutor{name='" + name + "', size=" + executors.length + "}";
    }

}
