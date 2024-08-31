/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

final class BasicCompleteTuple extends AbstractCompleteTuple {

    private BasicCompleteTuple() {
    }

    private BasicCompleteTuple(Literal label, Complete[] values) {
        restore(label, values);
    }

    static CompleteTuple createPrivatelyForKlvm(Literal label, Complete[] values) {
        return new BasicCompleteTuple(label, values);
    }

    static BasicCompleteTuple instanceForRestore() {
        return new BasicCompleteTuple();
    }

    @Override
    public final int unificationPriority() {
        return UnificationPriority.COMPLETE_TUPLE;
    }

}
