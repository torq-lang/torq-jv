/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.server.Server;
import org.torqlang.local.ConsoleLogger;

public class CoreServer {

    private static final ConsoleLogger LOGGER = ConsoleLogger.SINGLETON;

    private final Server server;
    private final int port;

    public CoreServer(Server server, int port) {
        this.server = server;
        this.port = port;
    }

    public static CoreServerBuilder builder() {
        return new CoreServerBuilder();
    }

    public final void join() throws InterruptedException {
        server.join();
    }

    public final void start() throws Exception {
        LOGGER.info("Starting server on port " + port);
        server.start();
        LOGGER.info("Started server on port " + port);
    }

    public final void stop() throws Exception {
        server.stop();
    }

}
