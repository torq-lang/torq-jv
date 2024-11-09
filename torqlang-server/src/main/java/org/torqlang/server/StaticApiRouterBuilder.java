/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorImage;
import org.torqlang.local.ActorRef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StaticApiRouterBuilder {

    private final List<ApiRoute> routes = new ArrayList<>();

    public final StaticApiRouterBuilder addRoute(String pathExpr, ActorImage actorImage, ApiDesc desc) {
        ApiPath path = new ApiPath(pathExpr);
        routes.add(new ApiRoute(path, actorImage, desc));
        return this;
    }

    public final StaticApiRouterBuilder addRoute(String pathExpr, ActorRef actorRef, ApiDesc desc) {
        ApiPath path = new ApiPath(pathExpr);
        routes.add(new ApiRoute(path, actorRef, desc));
        return this;
    }

    public final ApiRouter build() {
        routes.sort(Comparator.comparing(a -> a.path));
        return new StaticApiRouter(routes.toArray(new ApiRoute[0]));
    }

}
