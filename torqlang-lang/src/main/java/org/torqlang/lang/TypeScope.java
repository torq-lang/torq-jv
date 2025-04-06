/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

/*
 * A language type assignment that must be processed by the inference algorithm.
 */
public final class TypeScope {

    private final TypeEnv typeEnv;
    private final MonoInfr monoType;

    TypeScope(TypeEnv typeEnv, MonoInfr monoType) {
        this.typeEnv = typeEnv;
        this.monoType = monoType;
    }

    // TODO: Rename to `rho`
    public final MonoInfr monoType() {
        return monoType;
    }

    public final TypeEnv typeEnv() {
        return typeEnv;
    }
}
