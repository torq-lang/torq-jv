/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

public interface StreamClientResponse {
    Address address();

    Queue<Envelope> awaitEof(long timeout, TimeUnit unit) throws Exception;

    Queue<Envelope> mailbox();
}
