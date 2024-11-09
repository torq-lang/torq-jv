/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.Feature;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecDescBuilder {
    public final List<Map.Entry<Feature, ValueDesc>> entries;

    public RecDescBuilder() {
        this.entries = new ArrayList<>();
    }

    public final RecDescBuilder add(Feature key, ValueDesc value) {
        this.entries.add(Map.entry(key, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    public final RecDesc build() {
        Map.Entry<Feature, ValueDesc>[] array =
            (Map.Entry<Feature, ValueDesc>[]) Array.newInstance(Map.Entry.class, 0);
        array = this.entries.toArray(array);
        return new RecDesc(Map.ofEntries(array));
    }

}
