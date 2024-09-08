/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.FailedValue;
import org.torqlang.local.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.torqlang.examples.NorthwindTools.ROOT_DIR;
import static org.torqlang.examples.NorthwindTools.fetchColl;

/*
 * A read-through cache acts as an intermediary between an application and a database. When an application reads from
 * the cache, the cache checks if the data is already in memory. If the data is there, the cache returns it directly to
 * the application. If the data is not in memory, the cache loads it from the database, stores it in memory, and then
 * returns it to the application.
 *
 * The Northwind data is stored in the `/home/USER/.torq_lang/northwind` directory.
 */
public final class NorthwindReader extends AbstractActor {

    private final Map<String, List<Map<String, Object>>> cache;
    private final int latencyInNanos;

    NorthwindReader(Address address, Mailbox mailbox, Executor executor, Logger logger,
                    Map<String, List<Map<String, Object>>> cache, int latencyInNanos)
    {
        super(address, mailbox, executor, logger);
        this.cache = cache;
        this.latencyInNanos = latencyInNanos;
    }

    NorthwindReader(Address address, ActorSystem system, Map<String, List<Map<String, Object>>> cache, int latencyInNanos) {
        this(address, system.createMailbox(), system.executor(), system.createLogger(), cache, latencyInNanos);
    }

    @Override
    protected OnMessageResult onMessage(Envelope[] next) {
        // Native actors always receive 1 envelope
        Envelope envelope = next[0];
        Object message = envelope.message();
        try {
            if (latencyInNanos > 0) {
                Thread.sleep(0, latencyInNanos);
            }
            if (envelope.isRequest()) {
                // 1. Check if the data is already in memory
                // 2. If data is present, return it immediately to the requester
                // 3. If not present, read it from storage, cache it in memory, return it to the requester
                if (message instanceof ReadById readById) {
                    Map<String, Object> rec = NorthwindTools.fetchRec(cache, ROOT_DIR, readById.collName, readById.id);
                    sendResponseToBoth(envelope, rec);
                } else if (message instanceof ReadAll readAll) {
                    List<Map<String, Object>> coll = fetchColl(cache, ROOT_DIR, readAll.collName);
                    sendResponseToBoth(envelope, coll);
                } else {
                    throw new IllegalArgumentException("Unrecognized request: " + envelope);
                }
            } else if (envelope.isResponse()) {
                throw new IllegalArgumentException("Unrecognized response: " + envelope);
            } else {
                throw new IllegalArgumentException("Unrecognized message: " + envelope);
            }
        } catch (Throwable throwable) {
            String owner = address().toString();
            FailedValue failedValue = FailedValue.create(owner, throwable);
            if (envelope.requester() != null) {
                sendResponseToBoth(envelope, failedValue);
            } else {
                logError("NorthwindCacheReader error:\n" + failedValue.toDetailsString());
            }
        }
        // NorthwindCacheReader is self-healing and always running
        return OnMessageResult.NOT_FINISHED;
    }

    private void sendResponseToBoth(Envelope request, Object result) {
        ClientRequest clientRequest = (ClientRequest) request.message();
        Envelope response = Envelope.createResponse(result, request.requestId());
        request.requester().send(response);
        clientRequest.clientRequester().send(response);
    }

    interface ClientRequest {
        ActorRef clientRequester();
    }

    record ReadById(String collName, long id, ActorRef clientRequester) implements ClientRequest {
    }

    record ReadAll(String collName, ActorRef clientRequester) implements ClientRequest {
    }

}
