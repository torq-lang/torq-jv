/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class TupleDesc implements ValueDesc {
    private final List<ValueDesc> descs;

    public static TupleDesc of(ValueDesc v1) {
        return new TupleDesc(List.of(v1));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2) {
        return new TupleDesc(List.of(v1, v2));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3) {
        return new TupleDesc(List.of(v1, v2, v3));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4) {
        return new TupleDesc(List.of(v1, v2, v3, v4));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5, ValueDesc v6) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5, v6));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5, ValueDesc v6, ValueDesc v7) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5, v6, v7));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5, ValueDesc v6, ValueDesc v7, ValueDesc v8) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5, v6, v7, v8));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5, ValueDesc v6, ValueDesc v7, ValueDesc v8, ValueDesc v9) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5, v6, v7, v8, v9));
    }

    public static TupleDesc of(ValueDesc v1, ValueDesc v2, ValueDesc v3, ValueDesc v4, ValueDesc v5, ValueDesc v6, ValueDesc v7, ValueDesc v8, ValueDesc v9, ValueDesc v10) {
        return new TupleDesc(List.of(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
    }

    public TupleDesc(List<ValueDesc> descs) {
        this.descs = List.copyOf(descs);
    }

    public final List<ValueDesc> descs() {
        return descs;
    }
}
