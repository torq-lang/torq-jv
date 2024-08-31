/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class FailedValueError extends MachineError {

    public final FailedValue touchedFailedValue;

    public FailedValueError(FailedValue touchedFailedValue) {
        super("Failed value error");
        this.touchedFailedValue = touchedFailedValue;
    }

    @Override
    final ComputeHalt asComputeHalt(Stack current) {
        return new ComputeHalt(touchedFailedValue, current);
    }

}
