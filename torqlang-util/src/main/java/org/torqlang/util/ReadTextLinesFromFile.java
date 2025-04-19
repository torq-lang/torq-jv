/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ReadTextLinesFromFile {

    public static List<String> apply(String filePath) throws IOException {
        return apply(filePath, line -> line);
    }

    public static <T> List<T> apply(String filePath, Function<String, T> mapper) throws IOException {
        List<T> answer = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                answer.add(mapper.apply(line));
            }
        }
        return answer;
    }

}
