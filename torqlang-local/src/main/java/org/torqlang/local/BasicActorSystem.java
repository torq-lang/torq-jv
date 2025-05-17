/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.util.MapTools;

import java.util.Map;
import java.util.concurrent.Executor;

public final class BasicActorSystem implements ActorSystem {

    private final String name;
    private final Executor executor;
    private final Map<Address, ActorRefObj> actorsByAddress;
    private final Map<String, CompleteRec> packagesByPath;

    BasicActorSystem(String name,
                     Executor executor,
                     Map<Address, ActorRefObj> actorsByAddress,
                     Map<String, CompleteRec> packagesByPath)
    {
        this.name = name;
        this.executor = executor != null ?
            executor : ActorSystemDefaults.executor();
        this.actorsByAddress = MapTools.nullSafeCopyOf(actorsByAddress);
        this.packagesByPath = MapTools.nullSafeCopyOf(packagesByPath);
    }

    @Override
    public final ActorRefObj actorAt(Address address) {
        ActorRefObj actorRefObj = actorsByAddress.get(address);
        if (actorRefObj == null) {
            throw new ActorNotFoundError(address);
        }
        return actorRefObj;
    }

    @Override
    public final Logger createLogger() {
        return Logger.createDefault();
    }

    @Override
    public final Mailbox createMailbox() {
        return Mailbox.createDefault();
    }

    @Override
    public final Executor executor() {
        return executor;
    }

    @Override
    public final CompleteRec packageAt(String path) {
        CompleteRec pack = packagesByPath.get(path);
        if (pack == null) {
            throw new PackageNotFoundError(path);
        }
        return pack;
    }

    @Override
    public final String name() {
        return name;
    }

}
