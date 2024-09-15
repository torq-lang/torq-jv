/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.FailedValue;
import org.torqlang.local.*;

import java.io.IOException;
import java.util.concurrent.Executor;

public abstract class NorthwindAccessor extends AbstractActor {

    private final NorthwindCache cache;
    private final int latencyInNanos;

    protected NorthwindAccessor(Address address,
                                Mailbox mailbox,
                                Executor executor,
                                Logger logger,
                                NorthwindCache cache,
                                int latencyInNanos)
    {
        super(address, mailbox, executor, logger);
        this.cache = cache;
        this.latencyInNanos = latencyInNanos;
    }

    protected final NorthwindCache cache() {
        return cache;
    }

    protected abstract void onRequest(Envelope next) throws IOException;

    @Override
    protected final OnMessageResult onMessage(Envelope[] next) {
        // Unless overridden, native actors always receive 1 envelope
        Envelope envelope = next[0];
        try {
            if (latencyInNanos > 0) {
                Thread.sleep(0, latencyInNanos);
            }
            if (envelope.isRequest()) {
                onRequest(envelope);
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
        // NorthwindAccessor is self-healing and always running
        return OnMessageResult.NOT_FINISHED;
    }

    protected final void sendResponseToBoth(Envelope request, Object result) {
        ClientRequest clientRequest = (ClientRequest) request.message();
        Envelope response = Envelope.createResponse(result, request.requestId());
        request.requester().send(response);
        clientRequest.clientRequester().send(response);
    }

    interface ClientRequest {
        ActorRef clientRequester();
    }

}
