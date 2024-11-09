/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorSystem;

public final class ApiHandlerBuilder {

    private ActorSystem system;
    private ApiRouter router;

    ApiHandlerBuilder() {
    }

    public ApiHandler build() {
        return new ApiHandler(system, router);
    }

    public final ApiHandlerBuilder setRouter(ApiRouter router) {
        this.router = router;
        return this;
    }

    public final ApiHandlerBuilder setSystem(ActorSystem system) {
        this.system = system;
        return this;
    }

}
