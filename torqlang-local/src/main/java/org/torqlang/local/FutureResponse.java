/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.concurrent.CompletableFuture;

/*
 * FutureResponse is a single-send actor for the sole purpose of receiving a response. If you send it anything other
 * than a response, an IllegalArgumentException is thrown. Typically, the actor that sends a request is the receiver
 * of the response, but not always. In the exceptional case when the requester is not the receiver, FutureResponse can
 * be used.
 */
public final class FutureResponse implements ActorRef {

    private final Address address;
    private final CompletableFuture<Envelope> future;

    public FutureResponse(Address address) {
        this.address = address;
        this.future = new CompletableFuture<>();
    }

    @Override
    public final Address address() {
        return address;
    }

    public final CompletableFuture<Envelope> future() {
        return future;
    }

    @Override
    public final void send(Envelope envelope) {
        if (envelope.isResponse()) {
            future.complete(envelope);
        } else {
            throw new IllegalArgumentException("FutureEnvelope can not receive a notify or request");
        }
    }

}
