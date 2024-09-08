/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.examples.NorthwindReader.ReadAll;
import org.torqlang.examples.NorthwindReader.ReadById;
import org.torqlang.examples.NorthwindWriter.Write;
import org.torqlang.klvm.FailedValue;
import org.torqlang.local.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.torqlang.local.Envelope.createResponse;

public final class NorthwindDb extends AbstractActor {

    private final Map<String, List<Map<String, Object>>> cache;
    private final NorthwindReader[] readers;
    private final NorthwindWriter writer;

    private int activeReaders = 0;
    private boolean activeWriter;
    private int nextReader = 0;

    NorthwindDb(Address address, ActorSystem system, int concurrencyLevel, int readLatencyInNanos) {
        super(address, system.createMailbox(), system.executor(), system.createLogger());
        if (concurrencyLevel < 2) {
            throw new IllegalArgumentException("concurrencyLevel < 2");
        }
        cache = new HashMap<>();
        readers = new NorthwindReader[concurrencyLevel - 1];
        for (int i = 0; i < readers.length; i++) {
            Address childAddress = Address.create(address, "reader" + i);
            readers[i] = new NorthwindReader(childAddress, system, cache, readLatencyInNanos);
        }
        Address childAddress = Address.create(address, "writer");
        writer = new NorthwindWriter(childAddress, system, cache);
    }

    /*
     * An actor is executable when it can select a message for processing, and a
     * northwind database message can be processed when it is:
     *     1) A response
     *     2) A read request and `activeWriter` is false
     *     3) A write request and `activeReaders` is zero
     * Otherwise, we wait for responses
     */
    @Override
    protected boolean isExecutable(Mailbox mailbox) {
        Envelope envelope = mailbox.peekNext();
        if (envelope == null) {
            return false;
        }
        if (envelope.isResponse()) {
            return true;
        }
        Object message = envelope.message();
        if (message instanceof FindById || message instanceof FindAll) {
            return !activeWriter;
        }
        if (message instanceof Update) {
            return activeReaders == 0;
        }
        return false;
    }

    @Override
    protected Envelope[] selectNext(Mailbox mailbox) {
        Envelope envelope = mailbox.removeNext();
        // Process requests or notifications one at a time
        if (!envelope.isResponse()) {
            return new Envelope[]{envelope};
        }
        // Process multiple adjacent responses if possible
        Envelope peekNext = mailbox.peekNext();
        if (peekNext == null || !peekNext.isResponse()) {
            return new Envelope[]{envelope};
        }
        // We have at least two responses, so accumulate multiple responses
        ArrayList<Envelope> list = new ArrayList<>();
        list.add(envelope);
        do {
            list.add(mailbox.removeNext());
            peekNext = mailbox.peekNext();
        } while (peekNext != null && peekNext.isResponse());
        return list.toArray(new Envelope[0]);
    }

    @Override
    protected OnMessageResult onMessage(Envelope[] next) {
        for (Envelope envelope : next) {
            try {
                Object message = envelope.message();
                if (envelope.isRequest()) {
                    if (message instanceof Update databaseUpdate) {
                        Write cacheWrite = new Write(databaseUpdate.collName(),
                            databaseUpdate.data, envelope.requester());
                        writer.send(Envelope.createRequest(cacheWrite, this, cacheWrite));
                    } else if (message instanceof FindById databaseFindById) {
                        ReadById cacheReadById =
                            new ReadById(databaseFindById.collName, databaseFindById.id,
                                envelope.requester());
                        readers[nextReader].send(Envelope.createRequest(cacheReadById, this, cacheReadById));
                        activeReaders++;
                        nextReader++;
                        if (nextReader == readers.length) {
                            nextReader = 0;
                        }
                    } else {
                        throw new IllegalArgumentException("Unrecognized request: " + envelope);
                    }
                } else if (envelope.isResponse()) {
                    Object requestId = envelope.requestId();
                    if (requestId instanceof Write) {
                        activeWriter = false;
                    } else if (requestId instanceof ReadById || requestId instanceof ReadAll) {
                        activeReaders--;
                    } else {
                        throw new IllegalArgumentException("Unrecognized response: " + envelope);
                    }
                } else {
                    throw new IllegalArgumentException("Unrecognized message: " + envelope);
                }
            } catch (Throwable throwable) {
                String owner = address().toString();
                FailedValue failedValue = FailedValue.create(owner, throwable);
                if (envelope.requester() != null) {
                    envelope.requester().send(createResponse(failedValue, envelope.requestId()));
                } else {
                    logError("NorthwindDb error:\n" + failedValue.toDetailsString());
                }
            }
        }
        // NorthwindDB is self-healing and always running
        return OnMessageResult.NOT_FINISHED;
    }

    record FindById(String collName, long id) {
    }

    record FindAll(String collName) {
    }

    record Update(String collName, Map<String, Object> data) {
    }

}
