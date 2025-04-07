/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.util.GetStackTrace;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.torqlang.local.OnMessageResult.NOT_FINISHED;

/*
 * Actors are typically run in one of two strategies:
 * 1. An actor per core
 * 2. Many actors per core
 *
 * An actor per core -- Explicit IDs are provided in actor-per-core strategy. Consider a runtime consisting of four
 * cores. To implement the actor-per-core strategy, create four actors with identifiers 0-3, and an AffinityExecutor
 * with a size of 4. The explicit IDs create a perfect distribution across the 4 threads contained within the
 * AffinityExecutor.
 *
 * Many actors per core -- Explicit IDs are not provided in a many-actors-per-core strategy. Instead, the actors
 * default their ID to their identity hash code. Consider a runtime consisting of four cores for running thousands of
 * actors. To implement the many-actors-per-core strategy, create an AffinityExecutor with a size of 4. The identity
 * hash codes approximate an even distribution across the 4 threads contained within the AffinityExecutor, dynamically
 * partitioning the actors across the available threads.
 *
 * The actor-per-core strategy is typically used to implement long-running actors, such as I/O services.
 * The many-actors-per-core strategy is typically used to implement short-lived actors, such as REST handlers.
 */
public abstract class AbstractActor implements ActorRef {

    /*
     * Concurrency invariants:
     *     1. All access to the mailbox value must be synchronized on mailboxLock
     *     2. All access to the state value must be synchronized on mailboxLock
     */

    private final int id;
    private final Address address;
    private final Executor executor;
    private final Dispatcher dispatcher = new Dispatcher();
    private final Logger logger;
    private final Mailbox mailbox;
    private final Object mailboxLock = new Object();

    private volatile State state = State.WAITING;

    protected AbstractActor(int id, Address address, Mailbox mailbox, Executor executor, Logger logger) {
        this.id = id == Integer.MIN_VALUE ? System.identityHashCode(this) : id;
        this.address = address;
        this.mailbox = mailbox;
        this.executor = executor;
        this.logger = logger;
    }

    protected AbstractActor(Address address, Mailbox mailbox, Executor executor, Logger logger) {
        // We cannot call `System.identityHashCode(this)` here, so we pass `Integer.MIN_VALUE` as a sentinel value
        this(Integer.MIN_VALUE, address, mailbox, executor, logger);
    }

    public final Address address() {
        return address;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractActor that = (AbstractActor) other;
        return id == that.id;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    protected boolean isExecutable(Mailbox mailbox) {
        return !mailbox.isEmpty();
    }

    protected void logError(String message) {
        logger.error(address().toString(), message);
    }

    protected void logInfo(String message) {
        logger.info(address().toString(), message);
    }

    protected final Logger logger() {
        return logger;
    }

    /**
     * Should only be used for debugging and tracing.
     */
    protected final int mailboxSize() {
        synchronized (mailboxLock) {
            return mailbox.size();
        }
    }

    protected abstract OnMessageResult onMessage(Envelope[] next);

    protected void onReceivedAfterFailed(Envelope envelope) {
        logger.error(address.toString(), String.format("Message received after FAILED: %s", envelope));
    }

    protected void onReceivedAfterSuccessful(Envelope envelope) {
        logger.error(address.toString(), String.format("Message received after SUCCESSFUL: %s", envelope));
    }

    protected void onRejectedByExecutor(RejectedExecutionException exc) {
        logger.error(address.toString(), String.format("Actor rejected by executor:\n%s",
            GetStackTrace.apply(exc, true)));
    }

    /*
     * INVARIANT: The mailbox is locked during this call so that implementations can empty the mailbox while responding
     * to pending requests with the error.
     */
    protected void onUnhandledError(Mailbox mailbox, Throwable throwable) {
        logger.error(address.toString(), String.format("Unhandled error\n" +
            GetStackTrace.apply(throwable, true)));
    }

    protected OnMessageResult onUnrecognizedMessage(Envelope envelope) {
        logger.error(address.toString(), String.format("Unrecognized message: %s", envelope));
        return NOT_FINISHED;
    }

    protected Envelope[] selectNext(Mailbox mailbox) {
        return new Envelope[]{mailbox.remove()};
    }

    @Override
    public final void send(Envelope envelope) {
        synchronized (mailboxLock) {
            if (state == State.FAILED) {
                onReceivedAfterFailed(envelope);
            } else if (state == State.SUCCESSFUL) {
                onReceivedAfterSuccessful(envelope);
            } else {
                mailbox.add(envelope);
                // If we are ACTIVE, SCHEDULED, or WAITING-not-executable, there is nothing to do. However, if we are
                // WAITING-executable, we must schedule for execution.
                if (state == State.WAITING && isExecutable(mailbox)) {
                    dispatcher.schedule();
                }
            }
        }
    }

    public final State state() {
        synchronized (mailboxLock) {
            return state;
        }
    }

    public enum State {
        WAITING,        // actor is NOT executable (mailbox is empty or no selectable message in mailbox)
        SCHEDULED,      // actor is executable and actor is queued for execution
        ACTIVE,         // actor is currently processing a message (messages may arrive during processing)
        SUCCESSFUL,     // actor finished normally and will no longer accept mail
        FAILED          // actor finished abnormally and will no longer accept mail
    }

    private final class Dispatcher implements Runnable {

        private int actorId() {
            return id;
        }

        @Override
        public final boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Dispatcher that = (Dispatcher) other;
            return actorId() == that.actorId();
        }

        @Override
        public final int hashCode() {
            return id;
        }

        @Override
        public final void run() {
            // Because we have just been invoked by the executor, we know we are in the SCHEDULED state.
            try {
                Envelope[] next;
                synchronized (mailboxLock) {
                    // A message can be selected because we were previously scheduled as "executable", and now we
                    // are running. We must transition from SCHEDULED to ACTIVE as soon as we select a message from
                    // the mailbox.
                    next = selectNext(mailbox);
                    state = State.ACTIVE;
                }
                // CRITICAL: Do not synchronize on the mailboxLock during onMessage(). Releasing the lock allows
                // messages to be received while processing the current message.
                OnMessageResult result = onMessage(next);
                synchronized (mailboxLock) {
                    if (result == OnMessageResult.FINISHED) {
                        state = State.SUCCESSFUL;
                        return;
                    }
                    // We just completed processing of a single message, and we are not finished. We must transition
                    // from ACTIVE to either SCHEDULED or WAITING.
                    if (isExecutable(mailbox)) {
                        schedule();
                    } else {
                        state = State.WAITING;
                    }
                }
            } catch (Throwable throwable) {
                synchronized (mailboxLock) {
                    // We have just been interrupted by an unhandled error. We must transition from ACTIVE to FAILED.
                    state = State.FAILED;
                    onUnhandledError(mailbox, throwable);
                }
            }
        }

        /*
         * Must be called from within a "synchronized {...}" block
         */
        private void schedule() {
            try {
                state = State.SCHEDULED;
                executor.execute(this);
            } catch (RejectedExecutionException exc) {
                state = State.FAILED;
                onRejectedByExecutor(exc);
            }
        }
    }

}
