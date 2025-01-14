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
     * Return null if the requested option is not found. Otherwise, return its arguments.
     *
     * Throw an IllegalArgumentException if a duplicate option is found.
     */
    public static List<String> get(String option, List<String> args) {
        List<String> argsAtOpt = null;
        int i = 0;
        while (i < args.size()) {
            if (args.get(i).equals(option)) {
                argsAtOpt = new ArrayList<>();
                while (++i < args.size()) {
                    String a = args.get(i);
                    if (a.startsWith("-")) {
                        break;
                    }
                    argsAtOpt.add(a);
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
        return argsAtOpt != null ? List.copyOf(argsAtOpt) : null;
    }

    public static List<String> getEither(String option1, String option2, List<String> args) {
        List<String> answer = get(option1, args);
        if (answer == null) {
            answer = get(option2, args);
        }
        return answer;
    }

    public static String getSingle(String option, List<String> args) {
        List<String> argsAtOpt = GetArg.get(option, args);
        if (argsAtOpt == null || argsAtOpt.isEmpty()) {
            return null;
        }
        if (argsAtOpt.size() > 1) {
            throw new IllegalArgumentException("More than one argument found for option: " + option);
        }
        return argsAtOpt.get(0);
    }

    public static String getSingleFromEither(String option1, String option2, List<String> args) {
        String answer = getSingle(option1, args);
        if (answer == null) {
            answer = getSingle(option2, args);
        }
        return answer;
    }

}
