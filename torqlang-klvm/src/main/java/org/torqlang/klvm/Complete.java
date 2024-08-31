/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public interface Complete extends Value, CompleteOrIdent {

    @Override
    default Complete checkComplete() {
        return this;
    }

    @Override
    default Var toVar(Env env) {
        return new Var(this);
    }

    default Object toNativeValue() {
        throw new IllegalArgumentException("Cannot convert to native value: " + getClass().getName());
    }

}
