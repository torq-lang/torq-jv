/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public final class TypeUnificationError extends RuntimeException {

    public static final String TYPE_UNIFICATION_ERROR = "Type unification error";

    public final MonoType a;
    public final MonoType b;

    public TypeUnificationError(MonoType a, MonoType b) {
        super(TYPE_UNIFICATION_ERROR);
        this.a = a;
        this.b = b;
    }

}
