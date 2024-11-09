/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.Feature;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class RecDesc implements ValueDesc {
    public final Map<Feature, ValueDesc> map;

    public static RecDescBuilder builder() {
        return new RecDescBuilder();
    }

    public static RecDesc of(Feature k1, ValueDesc v1) {
        return new RecDesc(Map.of(k1, v1));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2) {
        return new RecDesc(Map.of(k1, v1, k2, v2));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5, Feature k6, ValueDesc v6) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5, Feature k6, ValueDesc v6, Feature k7, ValueDesc v7) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5, Feature k6, ValueDesc v6, Feature k7, ValueDesc v7, Feature k8, ValueDesc v8) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5, Feature k6, ValueDesc v6, Feature k7, ValueDesc v7, Feature k8, ValueDesc v8, Feature k9, ValueDesc v9) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
    }

    public static RecDesc of(Feature k1, ValueDesc v1, Feature k2, ValueDesc v2, Feature k3, ValueDesc v3, Feature k4, ValueDesc v4, Feature k5, ValueDesc v5, Feature k6, ValueDesc v6, Feature k7, ValueDesc v7, Feature k8, ValueDesc v8, Feature k9, ValueDesc v9, Feature k10, ValueDesc v10) {
        return new RecDesc(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
    }

    public RecDesc(Map<Feature, ValueDesc> map) {
        this.map = Map.copyOf(map);
    }
}
