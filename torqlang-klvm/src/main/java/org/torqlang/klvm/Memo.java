/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class Memo {

    private final Value a;
    private final Value b;

    public Memo(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Memo otherMemo = (Memo) other;
        return this.a == otherMemo.a && this.b == otherMemo.b;
    }

    public final int hashCode() {
        // Derived from Arrays.hashCode(Object[])
        int result = 1;
        result = result + System.identityHashCode(a);
        result = 31 * result + System.identityHashCode(b);
        return result;
    }

}
