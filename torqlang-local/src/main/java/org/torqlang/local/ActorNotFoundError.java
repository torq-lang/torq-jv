/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.MachineError;

public class ActorNotFoundError extends MachineError {
    public static final String ACTOR_NOT_FOUND = "Actor not found";
    public final Address address;

    public ActorNotFoundError(Address address) {
        super(ACTOR_NOT_FOUND);
        this.address = address;
    }
}
