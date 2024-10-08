/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

public interface Obj extends Composite {

    @Override
    default <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitObj(this, state);
    }

    @Override
    default Obj bindToValue(Value value, Set<Memo> memos) {
        if (this != value) {
            throw new UnificationError(this, value);
        }
        return this;
    }

    @Override
    default boolean entailsRec(Rec operand, Set<Memo> memos) {
        return false;
    }

    default String formatValue() {
        return getClass().getName();
    }

}
