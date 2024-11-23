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

import static org.torqlang.examples.NorthwindFiles.*;

/*
 * A write-through cache acts as an intermediary between an application and a database. When an application writes to
 * the cache, the cache writes the data to memory first and then immediately writes that cached data through to the
 * database. Processing suspends until the data is successfully written to the database.
 */
public class NorthwindWriter extends NorthwindAccessor {

    NorthwindWriter(int id,
                    Address address,
                    Mailbox mailbox,
                    Executor executor,
                    Logger logger,
                    NorthwindCache cache,
                    int latencyInNanos)
    {
        super(id, address, mailbox, executor, logger, cache, latencyInNanos);
    }

    NorthwindWriter(int id,
                    Address address,
                    ActorSystem system,
                    NorthwindCache cache,
                    int latencyInNanos)
    {
        this(id, address, system.createMailbox(), system.executor(), system.createLogger(), cache, latencyInNanos);
    }

    @Override
    protected void onRequest(Envelope next) throws IOException {
        Object message = next.message();
        // 1. Write the request data to memory
        // 2. Write the changed memory to the file system
        if (message instanceof WriteCreate writeCreate) {
            performCreate(writeCreate);
        } else if (message instanceof WriteDelete writeDelete) {
            performDelete(writeDelete);
        } else if (message instanceof WriteUpdate writeUpdate) {
            performUpdate(writeUpdate);
        } else {
            throw new IllegalArgumentException("Unrecognized request: " + next);
        }
    }

    private void performCreate(WriteCreate writeCreate) throws IOException {
        NorthwindColl coll = fetchColl(cache(), FILES_DIR, writeCreate.collName);
        Map<String, Object> key = extractKey(writeCreate.data, KEY_NAMES_BY_COLL.get(writeCreate.collName));
        Map<String, Object> rec = fetchRec(coll, key);
        if (rec != null) {
            throw new IllegalArgumentException("Record already exists: " + writeCreate.collName + " at " + key);
        }
        coll.list().add(writeCreate.data);
        saveColl(coll, FILES_DIR);
    }

    private void performDelete(WriteDelete writeDelete) throws IOException {
        NorthwindColl coll = fetchColl(cache(), FILES_DIR, writeDelete.collName);
        boolean removed = coll.list().removeIf(r -> NorthwindFiles.containsCriteria(r, writeDelete.key));
        if (!removed) {
            throw new IllegalArgumentException("Record not found: " + writeDelete.collName + " at " + writeDelete.key);
        }
        saveColl(coll, FILES_DIR);
    }

    private void performUpdate(WriteUpdate writeUpdate) throws IOException {
        NorthwindColl coll = fetchColl(cache(), FILES_DIR, writeUpdate.collName);
        Map<String, Object> key = extractKey(writeUpdate.data, KEY_NAMES_BY_COLL.get(writeUpdate.collName));
        Map<String, Object> rec = fetchRec(coll, key);
        if (rec == null) {
            throw new IllegalArgumentException("Record not found: " + writeUpdate.collName + " at " + key);
        }
        rec.putAll(writeUpdate.data);
        saveColl(coll, FILES_DIR);
    }

    interface Write extends DelegatedRequest {
    }

    record WriteCreate(String collName,
                       Map<String, Object> data,
                       ActorRef originalRequester,
                       Object originalRequestId)
        implements Write {
    }

    record WriteDelete(String collName,
                       Map<String, Object> key,
                       ActorRef originalRequester,
                       Object originalRequestId)
        implements Write {
    }

    record WriteUpdate(String collName,
                       Map<String, Object> data,
                       ActorRef originalRequester,
                       Object originalRequestId)
        implements Write {
    }

}
