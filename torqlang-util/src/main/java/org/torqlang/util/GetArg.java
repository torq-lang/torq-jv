/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.ArrayList;
import java.util.List;

public final class GetArg {

    /*
     * Return null if the requested option is not found. Otherwise, return the number of arguments following
     * that option.
     *
     * Throw an IllegalArgumentException if a duplicate option is found.
     */
    public static List<String> get(String option, List<String> args) {
        List<String> values = null;
        int i = 0;
        while (i < args.size()) {
            if (args.get(i).equals(option)) {
                values = new ArrayList<>();
                while (++i < args.size()) {
                    String a = args.get(i);
                    if (a.startsWith("-")) {
                        break;
                    }
                    values.add(a);
                }
                while (i < args.size()) {
                    if (args.get(i).equals(option)) {
                        throw new IllegalArgumentException("Duplicate option error: " + option);
                    }
                    i++;
                }
            } else {
                i++;
            }
        }
        return values != null ? List.copyOf(values) : null;
    }

}
