/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.IOException;
import java.net.URL;

public final class ReadTextFromResource {

    public static String apply(Class<?> reference, String absolutePath) throws IOException {
        URL url = reference.getResource(absolutePath);
        return ReadTextFromUrl.apply(url);
    }

}
