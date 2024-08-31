/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public abstract class WaitException extends Exception {

    public WaitException() {
        // Do NOT fill in the stack trace for wait exceptions
        super(null, null, true, false);
    }

    public abstract Object barrier();

}
