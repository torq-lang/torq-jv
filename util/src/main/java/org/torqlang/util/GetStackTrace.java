/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class GetStackTrace {

    public static String apply(Throwable throwable, boolean trimTrailingNewline) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            String answer = sw.toString();
            if (trimTrailingNewline && !answer.isEmpty() && answer.charAt(answer.length() - 1) == '\n') {
                answer = answer.substring(0, answer.length() - 1);
            }
            return answer;
        }
    }

}
