/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.ErrorWithSourceSpan;
import org.torqlang.util.SourceSpan;

import java.util.Objects;

public final class MachineHaltError extends ErrorWithSourceSpan {

    private final ComputeHalt computeHalt;

    public MachineHaltError(ComputeHalt computeHalt) {
        super(Objects.toString(computeHalt.uncaughtThrow), computeHalt.nativeCause);
        this.computeHalt = computeHalt;
    }

    public final ComputeHalt computeHalt() {
        return computeHalt;
    }

    public final Stack current() {
        return computeHalt.current;
    }

    public final Throwable nativeCause() {
        return computeHalt.nativeCause;
    }

    @Override
    public final SourceSpan sourceSpan() {
        return current().stmt;
    }

    public final FailedValue touchedFailedValue() {
        return computeHalt.touchedFailedValue;
    }

}
