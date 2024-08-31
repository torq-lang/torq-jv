/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

public class CommonTools {

    // NOTE: This method is duplicated at test org.torqlang.lang
    public static String stripCircularSpecifics(String kernelString) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < kernelString.length()) {
            char next = kernelString.charAt(i);
            if (next == '<' && i + 1 < kernelString.length() && kernelString.charAt(i + 1) == '<') {
                sb.append("<<$circular");
                i += 2;
                while (i < kernelString.length()) {
                    next = kernelString.charAt(i);
                    if (next == '>' && i + 1 < kernelString.length() && kernelString.charAt(i + 1) == '>') {
                        sb.append(">>");
                        i += 2;
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                sb.append(next);
                i++;
            }
        }
        return sb.toString();
    }

}
