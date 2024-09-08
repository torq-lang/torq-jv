/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/*
 * A write-through cache acts as an intermediary between an application and a database. When an application writes to
 * the cache, the cache writes the data to memory first and then immediately writes that cached data through to the
 * database. Processing suspends until the data is successfully written to the database.
 */
public class NorthwindWriter extends AbstractActor {

    private final Map<String, List<Map<String, Object>>> cache;

    NorthwindWriter(Address address, Mailbox mailbox, Executor executor, Logger logger,
                    Map<String, List<Map<String, Object>>> cache)
    {
        super(address, mailbox, executor, logger);
        this.cache = cache;
    }

    NorthwindWriter(Address address, ActorSystem system, Map<String, List<Map<String, Object>>> cache) {
        this(address, system.createMailbox(), system.executor(), system.createLogger(), cache);
    }

    @Override
    protected OnMessageResult onMessage(Envelope[] next) {
        throw new IllegalStateException("Not implemented");
    }

    record Write(String collection, Map<String, Object> data, ActorRef clientRequester) {
    }

}
