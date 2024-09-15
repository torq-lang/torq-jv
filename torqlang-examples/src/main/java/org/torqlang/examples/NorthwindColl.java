/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public final class NorthwindColl {

    private final String name;
    private final List<Map<String, Object>> list;

    public NorthwindColl(String name, List<Map<String, Object>> list) {
        this.name = name;
        this.list = list;
        // If this collection uses system generated IDs, capture the last ID used
        for (Map<String, Object> rec : list) {
            Long id = (Long) rec.get("id");
            if (id == null) {
                break;
            }
        }
    }

    public final List<Map<String, Object>> list() {
        return list;
    }

    public final String name() {
        return name;
    }
}
