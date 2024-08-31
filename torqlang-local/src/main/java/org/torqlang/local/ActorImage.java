/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.EnvEntry;
import org.torqlang.klvm.OpaqueValue;

public final class ActorImage extends OpaqueValue {
    public final ActorSystem system;
    public final EnvEntry askHandlerEntry;
    public final EnvEntry tellHandlerEntry;

    public ActorImage(ActorSystem system, EnvEntry askHandlerEntry, EnvEntry tellHandlerEntry) {
        this.system = system;
        this.askHandlerEntry = askHandlerEntry;
        this.tellHandlerEntry = tellHandlerEntry;
    }
}
