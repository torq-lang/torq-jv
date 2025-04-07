/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

/*
 * A simple and minimal interface to the concept of a Mailbox queue.
 */
public interface Mailbox {

    static Mailbox createDefault() {
        return new LinkedListMailbox(EnvelopeComparator.SINGLETON);
    }

    void add(Envelope envelope);

    boolean isEmpty();

    /**
     * Return the next message or null if mailbox is empty.
     */
    Envelope peek();

    /**
     * Remove and return the next message or null if mailbox is empty.
     */
    Envelope remove();

    int size();
}
