/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class UnmatchedJumpThrowError extends MachineError {

    public final JumpThrowInstr jumpThrowInstr;

    public UnmatchedJumpThrowError(JumpThrowInstr jumpThrowInstr) {
        super("Unmatched jump throw error");
        this.jumpThrowInstr = jumpThrowInstr;
    }

}
