/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class InvalidArgCountError extends MachineError {

    public final int minCount;
    public final int maxCount;
    public final List<CompleteOrIdent> args;
    public final Object receiver;

    public InvalidArgCountError(int expectedCount, List<CompleteOrIdent> args, Object receiver) {
        this(expectedCount, expectedCount, args, receiver);
    }

    public InvalidArgCountError(int minCount, int maxCount, List<CompleteOrIdent> args, Object receiver) {
        super("Invalid arg count");
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.args = args;
        this.receiver = receiver;
    }
}
