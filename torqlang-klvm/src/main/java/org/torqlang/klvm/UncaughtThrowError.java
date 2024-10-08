/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class UncaughtThrowError extends MachineError {

    public final Complete uncaughtThrow;
    public final Throwable nativeCause;

    public UncaughtThrowError(Complete uncaughtThrow, Throwable nativeCause) {
        super("Uncaught throw error");
        this.uncaughtThrow = uncaughtThrow;
        this.nativeCause = nativeCause;
    }

    @Override
    final ComputeHalt asComputeHalt(Stack current) {
        return new ComputeHalt(uncaughtThrow, current, nativeCause);
    }

}
