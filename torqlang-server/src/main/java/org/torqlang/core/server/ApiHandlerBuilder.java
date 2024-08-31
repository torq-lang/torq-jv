/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorSystem;
import org.torqlang.local.ApiRouter;

public final class ApiHandlerBuilder {

    private ActorSystem system;
    private ApiRouter apiRouter;
    private ContextProvider contextProvider;

    ApiHandlerBuilder() {
    }

    public ApiHandler build() {
        return new ApiHandler(system, apiRouter, contextProvider);
    }

    public final ApiHandlerBuilder setApiRouter(ApiRouter apiRouter) {
        this.apiRouter = apiRouter;
        return this;
    }

    public final ApiHandlerBuilder setContextProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        return this;
    }

    public final ApiHandlerBuilder setSystem(ActorSystem system) {
        this.system = system;
        return this;
    }

}
