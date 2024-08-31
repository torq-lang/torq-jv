/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.OpaqueValue;
import org.torqlang.klvm.ValueOrVar;

/**
 * A local-only reference to a ValueOrVar
 */
final class ValueOrVarRef extends OpaqueValue implements RequestId {
    final ValueOrVar valueOrVar;

    ValueOrVarRef(ValueOrVar valueOrVar) {
        this.valueOrVar = valueOrVar;
    }

    public final String toString() {
        return "ValueOrVarRef(" + valueOrVar + ")";
    }

}
