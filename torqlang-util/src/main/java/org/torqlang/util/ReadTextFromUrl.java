/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ReadTextFromUrl {

    public static String apply(URL url) throws IOException {
        try (InputStream s = url.openStream()) {
            return new String(s.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
