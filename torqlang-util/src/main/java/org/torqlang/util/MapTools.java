/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.Map;

public final class MapTools {
    public static <K, V> Map<K, V> nullSafeCopyOf(Map<K, V> map) {
        return map == null ? Map.of() : Map.copyOf(map);
    }
}
