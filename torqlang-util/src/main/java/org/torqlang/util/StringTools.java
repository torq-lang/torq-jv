/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public final class StringTools {

    public static String appendColumn(String first, String second) {
        List<String> c1 = toSourceLines(first, 0, 0);
        List<String> c2 = toSourceLines(second, 0, 0);
        List<String> c1Padded = new ArrayList<>(c1.size());
        int maxWidth = 0;
        for (String s : c1) {
            maxWidth = Math.max(maxWidth, s.length());
        }
        for (String s : c1) {
            StringBuilder sb = new StringBuilder(maxWidth);
            appendWithPadRight(s, ' ', maxWidth, sb);
            c1Padded.add(sb.toString());
        }
        int maxRows = Math.max(c1.size(), c2.size());
        StringBuilder answer = new StringBuilder();
        String sep = " | ";
        boolean lineAppended = false;
        for (int i = 0; i < maxRows; i++) {
            String left;
            if (i < c1Padded.size()) {
                left = c1Padded.get(i);
            } else {
                StringBuilder sb = new StringBuilder(maxWidth);
                appendWithPadRight("", ' ', maxWidth, sb);
                left = sb.toString();
            }
            String right;
            if (i < c2.size()) {
                right = c2.get(i);
            } else {
                right = "";
            }
            if (lineAppended) {
                answer.append('\n');
            }
            answer.append(left);
            answer.append(sep);
            answer.append(right);
            lineAppended = true;
        }
        return answer.toString();
    }

    public static void appendWithPadLeft(String arg, char pad, int width, StringBuilder sb) {
        int deficit = width - arg.length();
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < deficit; i++) {
            sb.append(pad);
        }
        sb.append(arg);
    }

    public static void appendWithPadLeft(String arg, char pad, int width, StringWriter sb) {
        int deficit = width - arg.length();
        for (int i = 0; i < deficit; i++) {
            sb.write(pad);
        }
        sb.write(arg);
    }

    public static void appendWithPadRight(String arg, char pad, int width, StringBuilder sb) {
        sb.append(arg);
        int deficit = width - arg.length();
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < deficit; i++) {
            sb.append(pad);
        }
    }

    public static void appendWithPadRight(String arg, char pad, int width, StringWriter sw) {
        sw.write(arg);
        int deficit = width - arg.length();
        for (int i = 0; i < deficit; i++) {
            sw.append(pad);
        }
    }

    /*
     * Append the given character as a four character hex string.
     */
    public static void appendHexString(char c, StringBuilder sb) {
        String hex = Integer.toHexString(c);
        appendWithPadLeft(hex, '0', 4, sb);
    }

    public static void appendHexString(char c, StringWriter sw) {
        String hex = Integer.toHexString(c);
        appendWithPadLeft(hex, '0', 4, sw);
    }

    public static String lineNrStr(int lineNr, int width) {
        if (width < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder(width);
        appendWithPadLeft(String.valueOf(lineNr), '0', width, sb);
        return sb.toString();
    }

    public static List<String> toSourceLines(String source, int baseLineNr, int lineNrWidth) {
        int lineNr = baseLineNr;
        List<String> answer = new ArrayList<>();
        StringBuilder sourceLine = null;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                String lineNrStr = lineNrStr(lineNr, lineNrWidth);
                if (sourceLine == null) {
                    answer.add(lineNrStr);
                } else {
                    if (!lineNrStr.isEmpty()) {
                        answer.add(lineNrStr + " " + sourceLine);
                    } else {
                        answer.add(sourceLine.toString());
                    }
                    sourceLine = null;
                }
                lineNr++;
            } else {
                if (sourceLine == null) {
                    sourceLine = new StringBuilder();
                }
                sourceLine.append(c);
            }
        }
        if (sourceLine != null) {
            String lineNrStr = lineNrStr(lineNr, lineNrWidth);
            if (!lineNrStr.isEmpty()) {
                answer.add(lineNrStr + " " + sourceLine);
            } else {
                answer.add(sourceLine.toString());
            }
        }
        return answer;
    }
}
