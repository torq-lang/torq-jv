/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.util.BinarySearchTools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class BasicActorSystem implements ActorSystem {

    private final String name;
    private final Executor executor;
    private final ActorEntry[] actorsByAddress;
    private final PackageEntry[] packagesByPath;

    BasicActorSystem(String name,
                     Executor executor,
                     Map<Address, ActorRefObj> actorsByAddress,
                     Map<String, CompleteRec> packagesByPath)
    {
        this.name = name;
        this.executor = executor != null ?
            executor : ActorSystemDefaults.executor();
        int i = 0;
        this.actorsByAddress = new ActorEntry[actorsByAddress.size()];
        for (Map.Entry<Address, ActorRefObj> entry : actorsByAddress.entrySet()) {
            this.actorsByAddress[i++] = new ActorEntry(entry.getKey(), entry.getValue());
        }
        Arrays.sort(this.actorsByAddress, Comparator.comparing(a -> a.address));
        i = 0;
        this.packagesByPath = new PackageEntry[packagesByPath.size()];
        for (Map.Entry<String, CompleteRec> entry : packagesByPath.entrySet()) {
            this.packagesByPath[i++] = new PackageEntry(entry.getKey(), entry.getValue());
        }
        Arrays.sort(this.packagesByPath, Comparator.comparing(p -> p.path));
    }

    @Override
    public final ActorRefObj actorAt(Address address) {
        int i = BinarySearchTools.search(actorsByAddress, a -> address.compareTo(a.address));
        if (i < 0) {
            throw new ActorNotFoundError(address);
        }
        return actorsByAddress[i].actorRefObj;
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
    public final CompleteRec packageAt(String qualifier) {
        int i = BinarySearchTools.search(packagesByPath, p -> qualifier.compareTo(p.path));
        if (i < 0) {
            throw new PackageNotFoundError(qualifier);
        }
        return packagesByPath[i].members;
    }

    @Override
    public final String name() {
        return name;
    }

    @SuppressWarnings("ClassCanBeRecord")
    static final class ActorEntry implements Comparable<ActorEntry> {
        public final Address address;
        public final ActorRefObj actorRefObj;

        public ActorEntry(Address address, ActorRefObj actorRefObj) {
            this.address = address;
            this.actorRefObj = actorRefObj;
        }

        @Override
        public final int compareTo(ActorEntry actorEntry) {
            return address.compareTo(actorEntry.address);
        }

        @Override
        public final boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            ActorEntry that = (ActorEntry) other;
            return Objects.equals(address, that.address);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(address);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    static final class PackageEntry implements Comparable<PackageEntry> {
        public final String path;
        public final CompleteRec members;

        public PackageEntry(String path, CompleteRec members) {
            this.path = path;
            this.members = members;
        }

        @Override
        public final int compareTo(PackageEntry moduleEntry) {
            return path.compareTo(moduleEntry.path);
        }

        @Override
        public final boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            PackageEntry that = (PackageEntry) other;
            return Objects.equals(path, that.path);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(path);
        }
    }

}
