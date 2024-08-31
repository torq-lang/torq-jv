/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class ActorEntry implements Comparable<ActorEntry> {
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
