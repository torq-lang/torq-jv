/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.torqlang.examples.NorthwindFiles.fetchColl;
import static org.torqlang.examples.NorthwindFiles.FILES_DIR;

/*
 * A read-through cache acts as an intermediary between an application and a database. When an application reads from
 * the cache, the cache checks if the data is already in memory. If the data is there, the cache returns it directly to
 * the application. If the data is not in memory, the cache loads it from the database, stores it in memory, and then
 * returns it to the application.
 *
 * The Northwind data is stored in the `/home/USER/.torq_lang/northwind` directory.
 */
public final class NorthwindReader extends NorthwindAccessor {

    NorthwindReader(int id,
                    Address address,
                    Mailbox mailbox,
                    Executor executor,
                    Logger logger,
                    NorthwindCache cache,
                    int latencyInNanos)
    {
        super(id, address, mailbox, executor, logger, cache, latencyInNanos);
    }

    NorthwindReader(int id,
                    Address address,
                    ActorSystem system,
                    NorthwindCache cache,
                    int latencyInNanos)
    {
        this(id, address, system.createMailbox(), system.executor(), system.createLogger(), cache, latencyInNanos);
    }

    @Override
    protected final void onRequest(Envelope next) throws IOException {
        Object message = next.message();
        // 1. Check if the data is already in memory
        // 2. If data is present, return it immediately to the requester
        // 3. If not present, read it from storage, cache it in memory, return it to the requester
        if (message instanceof ReadByKey readByKey) {
            Map<String, Object> rec = NorthwindFiles.fetchRec(cache(), FILES_DIR, readByKey.collName, readByKey.key);
            sendResponseToBoth(next, rec);
        } else if (message instanceof ReadAll readAll) {
            NorthwindColl coll = fetchColl(cache(), FILES_DIR, readAll.collName);
            if (readAll.criteria != null) {
                coll = NorthwindFiles.filterColl(coll, readAll.criteria);
            }
            sendResponseToBoth(next, coll.list());
        } else {
            throw new IllegalArgumentException("Unrecognized request: " + next);
        }
    }

    interface Read extends DelegatedRequest {
    }

    record ReadAll(String collName,
                   Map<String, Object> criteria,
                   ActorRef originalRequester,
                   Object originalRequestId)
        implements Read {
    }

    record ReadByKey(String collName,
                     Map<String, Object> key,
                     ActorRef originalRequester,
                     Object originalRequestId)
        implements Read {
    }

}
