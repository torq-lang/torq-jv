/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.Complete;

import java.util.concurrent.TimeUnit;

public interface RequestClientInit {
    Address address();

    RequestClientResponse send(ActorRef actorRef, CaptureImage message);

    RequestClientResponse send(ActorRef actorRef, Complete message);

    Object sendAndAwaitResponse(ActorRef actorRef, Complete message, long timeout, TimeUnit unit) throws Exception;

    RequestClientInit setAddress(Address address);
}