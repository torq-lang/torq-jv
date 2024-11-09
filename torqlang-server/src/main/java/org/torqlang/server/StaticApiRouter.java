/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.util.BinarySearchTools;

final class StaticApiRouter implements ApiRouter {

    private final ApiRoute[] routingTable;

    StaticApiRouter(ApiRoute[] routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public final ApiRoute findRoute(ApiPath path) {
        int index = BinarySearchTools.search(routingTable, (r) -> path.compareTo(r.path));
        if (index < 0) {
            return null;
        }
        return routingTable[index];
    }

}
