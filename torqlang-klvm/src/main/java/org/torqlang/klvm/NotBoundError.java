/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class NotBoundError extends MachineError {
    public final Var arg;
    public final Stmt stmt;

    public NotBoundError(Var arg, Stmt stmt) {
        super("Not bound error");
        this.arg = arg;
        this.stmt = stmt;
    }
}
