/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public final class LocalServerBuilder {

    private int port;
    private final ContextHandlerCollection contextHandlers = new ContextHandlerCollection(false);

    LocalServerBuilder() {
    }

    public final LocalServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public final int port() {
        return port;
    }

    public final LocalServerBuilder addContextHandler(Handler handler, String contextPath) {
        ContextHandler contextHandler = new ContextHandler(handler, contextPath);
        contextHandlers.addHandler(contextHandler);
        return this;
    }

    public LocalServer build() {
        Server server = new Server(port);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);
        server.setHandler(contextHandlers);
        return new LocalServer(server, port);
    }

}
