/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

@SuppressWarnings("ClassCanBeRecord")
public final class ArrayDesc implements ValueDesc {
    private final ValueDesc componentSpec;

    public ArrayDesc(ValueDesc componentSpec) {
        this.componentSpec = componentSpec;
    }

    public static ArrayDesc of(ValueDesc componentType) {
        return new ArrayDesc(componentType);
    }

    public final ValueDesc componentSpec() {
        return componentSpec;
    }
}
