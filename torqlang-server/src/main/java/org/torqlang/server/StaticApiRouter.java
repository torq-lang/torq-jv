/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.util.BinarySearchTools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class StaticApiRouter implements ApiRouter {

    private final ApiRoute[] routesByPath;

    StaticApiRouter(List<ApiRoute> routes) {
        this.routesByPath = routes.toArray(new ApiRoute[0]);
        Arrays.sort(routesByPath, Comparator.comparing(r -> r.path));
    }

    @Override
    public final ApiRoute findRoute(ApiPath path) {
        int index = BinarySearchTools.search(routesByPath, (r) -> path.compareTo(r.path));
        if (index < 0) {
            return null;
        }
        return routesByPath[index];
    }

}
