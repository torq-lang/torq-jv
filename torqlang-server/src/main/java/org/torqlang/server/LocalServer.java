/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.server.Server;
import org.torqlang.local.ConsoleLogger;

public class LocalServer {

    private static final ConsoleLogger LOGGER = ConsoleLogger.SINGLETON;

    private final Server server;
    private final int port;

    public LocalServer(Server server, int port) {
        this.server = server;
        this.port = port;
    }

    public static LocalServerBuilder builder() {
        return new LocalServerBuilder();
    }

    public final void join() throws InterruptedException {
        server.join();
    }

    public final void start() throws Exception {
        LOGGER.info("Starting server on port " + port);
        server.start();
        LOGGER.info("Done starting server");
        LOGGER.info("  Port: " + port);
        LOGGER.info("  Invocation type: " + server.getInvocationType());
    }

    public final void stop() throws Exception {
        server.stop();
    }

}
