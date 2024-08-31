/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public abstract class MachineError extends RuntimeException {

    public MachineError(String message) {
        super(message);
    }

    ComputeHalt asComputeHalt(Stack current) {
        return new ComputeHalt(current, this);
    }

}
