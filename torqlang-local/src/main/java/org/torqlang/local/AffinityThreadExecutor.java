/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class AffinityThreadExecutor implements Executor {

    private static final String THREAD_NAME_PREFIX = "torq-affinity-thread-";

    private final Worker worker;

    public AffinityThreadExecutor(int cpu) {
        worker = new Worker(cpu);
        worker.start();
    }

    @Override
    public void execute(Runnable runnable) throws RejectedExecutionException {
        try {
            worker.queue.put(runnable);
        } catch (InterruptedException exc) {
            throw new RejectedExecutionException("Interrupted", exc);
        }
    }

    private static final class Worker implements Runnable {

        private final int cpu;
        private final LinkedBlockingQueue<Runnable> queue;
        private final Thread thread;

        private Worker(int cpu) {
            this.cpu = cpu;
            this.queue = new LinkedBlockingQueue<>();
            this.thread = new Thread(this, THREAD_NAME_PREFIX + cpu);
        }

        @SuppressWarnings({"WhileCanBeDoWhile", "InfiniteLoopStatement"})
        @Override
        public final void run() {
            try {
                while (true) {
                    Runnable r = queue.poll();
                    if (r == null) {
                        r = queue.take();
                    }
                    r.run();
                }
            } catch (InterruptedException exc) {
                System.err.println("AffinityExecutor interrupted:\n" + exc);
            }
        }

        private void start() {
            thread.start();
        }
    }

}
