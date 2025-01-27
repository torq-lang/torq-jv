/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class NotBoolError extends MachineError {
    public final Value arg;
    public final Instr instr;

    public NotBoolError(Value arg, Instr instr) {
        super("Not a Bool");
        this.arg = arg;
        this.instr = instr;
    }
}
