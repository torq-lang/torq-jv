/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class WaitVarException extends WaitException {

    private final Var var;

    public WaitVarException(Var var) {
        this.var = var;
    }

    @Override
    public final Var barrier() {
        return var;
    }

    @Override
    public final String toString() {
        return "Wait exception at barrier: " + var;
    }

}
