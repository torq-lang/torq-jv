/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.Test;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Rec;
import org.torqlang.local.Actor;
import org.torqlang.local.ActorImage;
import org.torqlang.local.ActorSystem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestStaticApiRouter {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    private static final String SOURCE = """
        actor TestApi() in
            handle ask 'GET'#{'query': query} in
                null
            end
        end""";

    @Test
    public void test01() throws Exception {

        ApiDesc emptyApiDesc = ApiDesc.builder()
            .setContextProvider(TestStaticApiRouter::emptyContextProvider)
            .build();

        ActorImage testActorImage = Actor.builder()
            .setSystem(ActorSystem.defaultSystem())
            .actorImage(SOURCE);

        StaticApiRouter router;

        router = new StaticApiRouter(List.of());
        assertNull(router.findRoute(ApiPath.parse("/orders")));

        router = new StaticApiRouter(List.of(
            ApiRoute.create(ApiPath.parse("/orders"), testActorImage, emptyApiDesc)
        ));
        assertNotNull(router.findRoute(ApiPath.parse("/orders")));
        assertNull(router.findRoute(ApiPath.parse("/orders/1")));
        assertNull(router.findRoute(ApiPath.parse("/shippers")));

        router = new StaticApiRouter(List.of(
            ApiRoute.create(ApiPath.parse("/orders"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders/{id}"), testActorImage, emptyApiDesc)
        ));
        assertNotNull(router.findRoute(ApiPath.parse("/orders")));
        assertNull(router.findRoute(ApiPath.parse("/shippers")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders/1")));
        assertNull(router.findRoute(ApiPath.parse("/orders/1/releases")));

        router = new StaticApiRouter(List.of(
            ApiRoute.create(ApiPath.parse("/inventory"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders"), testActorImage, emptyApiDesc)
        ));
        assertNotNull(router.findRoute(ApiPath.parse("/inventory")));
        assertNull(router.findRoute(ApiPath.parse("/shippers")));
        assertNull(router.findRoute(ApiPath.parse("/inventory/1")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders")));

        router = new StaticApiRouter(List.of(
            ApiRoute.create(ApiPath.parse("/inventory"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/inventory/{id}"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders/{id}"), testActorImage, emptyApiDesc)
        ));
        assertNotNull(router.findRoute(ApiPath.parse("/orders")));
        assertNotNull(router.findRoute(ApiPath.parse("/inventory")));
        assertNull(router.findRoute(ApiPath.parse("/shippers")));
        assertNotNull(router.findRoute(ApiPath.parse("/inventory/1")));
        assertNull(router.findRoute(ApiPath.parse("/inventory/1/locations")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders/1")));
        assertNull(router.findRoute(ApiPath.parse("/orders/1/releases")));

        router = new StaticApiRouter(List.of(
            ApiRoute.create(ApiPath.parse("/orders"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders/{id}"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders/{id}/releases"), testActorImage, emptyApiDesc),
            ApiRoute.create(ApiPath.parse("/orders/{id}/releases/{id}"), testActorImage, emptyApiDesc)
        ));
        assertNotNull(router.findRoute(ApiPath.parse("/orders")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders/1")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders/1/releases")));
        assertNotNull(router.findRoute(ApiPath.parse("/orders/1/releases/1")));
        assertNull(router.findRoute(ApiPath.parse("/shippers")));
        assertNull(router.findRoute(ApiPath.parse("/orders/1/customer")));
        assertNull(router.findRoute(ApiPath.parse("/orders/1/customer/address")));
    }

}
