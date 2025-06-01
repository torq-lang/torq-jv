/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.examples.NorthwindReader.ReadAll;
import org.torqlang.examples.NorthwindReader.ReadByKey;
import org.torqlang.examples.NorthwindWriter.WriteCreate;
import org.torqlang.examples.NorthwindWriter.WriteDelete;
import org.torqlang.examples.NorthwindWriter.WriteUpdate;
import org.torqlang.klvm.FailedValue;
import org.torqlang.local.*;

import java.util.ArrayList;
import java.util.Map;

import static org.torqlang.local.Envelope.createResponse;

public final class NorthwindDb extends AbstractActor {

    private final NorthwindCache cache;
    private final NorthwindReader[] readers;
    private final NorthwindWriter writer;

    private int activeReaders = 0;
    private boolean activeWriter;
    private int nextReader = 0;

    NorthwindDb(Address address, ActorSystem system, int concurrency, int readLatency) {
        // ID = 0
        super(0, address, system.createMailbox(), system.executor(), system.createLogger());
        if (concurrency < 4) {
            throw new IllegalArgumentException("concurrency < 4");
        }
        cache = new NorthwindCache();
        Address writerAddress = Address.create(address, "writer");
        // ID = 1
        writer = new NorthwindWriter(1, writerAddress, system, cache, readLatency);
        logger().info("Writer created: " + writerAddress);
        readers = new NorthwindReader[concurrency - 2];
        for (int i = 0; i < readers.length; i++) {
            Address readerAddress = Address.create(address, "reader" + i);
            // IDs = 2 through (concurrency - 1)
            readers[i] = new NorthwindReader(i + 2, readerAddress, system, cache, readLatency);
            logger().info("Reader created: " + readerAddress);
        }
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
        Envelope envelope = mailbox.peek();
        if (envelope == null) {
            return false;
        }
        if (envelope.isResponse()) {
            return true;
        }
        Object message = envelope.message();
        if (message instanceof Reader) {
            return !activeWriter;
        } else if (message instanceof Writer) {
            return activeReaders == 0 && !activeWriter;
        } else {
            return false;
        }
    }

    @Override
    protected Envelope[] selectNext(Mailbox mailbox) {
        Envelope envelope = mailbox.remove();
        // Process requests or notifications one at a time
        if (!envelope.isResponse()) {
            return new Envelope[]{envelope};
        }
        // Process multiple adjacent responses if possible
        Envelope peekNext = mailbox.peek();
        if (peekNext == null || !peekNext.isResponse()) {
            return new Envelope[]{envelope};
        }
        // We have at least two responses, so accumulate multiple responses
        ArrayList<Envelope> list = new ArrayList<>();
        list.add(envelope);
        do {
            list.add(mailbox.remove());
            peekNext = mailbox.peek();
        } while (peekNext != null && peekNext.isResponse());
        return list.toArray(new Envelope[0]);
    }

    @Override
    protected OnMessageResult onMessage(Envelope[] next) {
        for (Envelope envelope : next) {
            try {
                Object message = envelope.message();
                if (envelope.isRequest()) {
                    if (message instanceof Reader) {
                        Object readRequest;
                        if (message instanceof FindByKey findByKey) {
                            readRequest = new ReadByKey(findByKey.collName, findByKey.key,
                                envelope.requester(), envelope.requestId());
                        } else if (message instanceof FindAll findAll) {
                            readRequest = new ReadAll(findAll.collName, findAll.criteria, envelope.requester(),
                                envelope.requestId());
                        } else {
                            throw new IllegalArgumentException("Unrecognized read request: " + envelope);
                        }
                        readers[nextReader].send(Envelope.createRequest(readRequest, this, readRequest));
                        activeReaders++;
                        nextReader++;
                        if (nextReader == readers.length) {
                            nextReader = 0;
                        }
                    } else {
                        Object writeRequest;
                        if (message instanceof Update update) {
                            writeRequest = new WriteUpdate(update.collName(), update.data, envelope.requester(),
                                envelope.requestId());
                        } else if (message instanceof Create create) {
                            writeRequest = new WriteCreate(create.collName(), create.data, envelope.requester(),
                                envelope.requestId());
                        } else if (message instanceof Delete delete) {
                            writeRequest = new WriteDelete(delete.collName(), delete.key, envelope.requester(),
                                envelope.requestId());
                        } else {
                            throw new IllegalArgumentException("Unrecognized write request: " + envelope);
                        }
                        writer.send(Envelope.createRequest(writeRequest, this, writeRequest));
                        activeWriter = true;
                    }
                } else if (envelope.isResponse()) {
                    Object requestId = envelope.requestId();
                    if (requestId instanceof NorthwindWriter.Write) {
                        activeWriter = false;
                    } else if (requestId instanceof NorthwindReader.Read) {
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

    interface Reader {
    }

    interface Writer {
    }

    record Create(String collName, Map<String, Object> data) implements Writer {
    }

    record Delete(String collName, Map<String, Object> key) implements Writer {
    }

    record FindAll(String collName, Map<String, Object> criteria) implements Reader {
    }

    record FindByKey(String collName, Map<String, Object> key) implements Reader {
    }

    record Update(String collName, Map<String, Object> data) implements Writer {
    }

}
