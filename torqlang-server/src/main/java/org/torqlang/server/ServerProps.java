/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import java.util.concurrent.ConcurrentHashMap;

public final class ServerProps {

    /*
        Because command line options can have long and short hyphenated names, server options are mapped to a single
        property name. For example, both "-r" and "--resources-root" are mapped to "org.torqlang.resources.root".
     */

    public static final String PORT_SHORT_OPTION = "-p";
    public static final String PORT_LONG_OPTION = "--port";
    public static final String PORT_PROP = "org.torqlang.server.port";

    public static final String RESOURCES_SHORT_OPTION = "-r";
    public static final String RESOURCES_LONG_OPTION = "--resources";
    public static final String RESOURCES_PROP = "org.torqlang.server.resources";

    private static final ConcurrentHashMap<String, Object> props = new ConcurrentHashMap<>();

    public static String getResourcesRoot() {
        return (String) get(RESOURCES_PROP);
    }

    public static Object get(String option) {
        return props.get(option);
    }

    public static Object put(String option, Object value) {
        return props.put(option, value);
    }

}
