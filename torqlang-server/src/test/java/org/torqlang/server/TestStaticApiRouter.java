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

        router = new StaticApiRouter(new ApiRoute[0]);
        assertNull(router.findRoute(new ApiPath("/orders")));

        router = new StaticApiRouter(new ApiRoute[]{
            new ApiRoute(new ApiPath("/orders"), testActorImage, emptyApiDesc)
        });
        assertNotNull(router.findRoute(new ApiPath("/orders")));
        assertNull(router.findRoute(new ApiPath("/orders/1")));
        assertNull(router.findRoute(new ApiPath("/shippers")));

        router = new StaticApiRouter(new ApiRoute[]{
            new ApiRoute(new ApiPath("/orders"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders/{id}"), testActorImage, emptyApiDesc)
        });
        assertNotNull(router.findRoute(new ApiPath("/orders")));
        assertNull(router.findRoute(new ApiPath("/shippers")));
        assertNotNull(router.findRoute(new ApiPath("/orders/1")));
        assertNull(router.findRoute(new ApiPath("/orders/1/releases")));

        router = new StaticApiRouter(new ApiRoute[]{
            new ApiRoute(new ApiPath("/inventory"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders"), testActorImage, emptyApiDesc)
        });
        assertNotNull(router.findRoute(new ApiPath("/inventory")));
        assertNull(router.findRoute(new ApiPath("/shippers")));
        assertNull(router.findRoute(new ApiPath("/inventory/1")));
        assertNotNull(router.findRoute(new ApiPath("/orders")));

        router = new StaticApiRouter(new ApiRoute[]{
            new ApiRoute(new ApiPath("/inventory"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/inventory/{id}"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders/{id}"), testActorImage, emptyApiDesc)
        });
        assertNotNull(router.findRoute(new ApiPath("/orders")));
        assertNotNull(router.findRoute(new ApiPath("/inventory")));
        assertNull(router.findRoute(new ApiPath("/shippers")));
        assertNotNull(router.findRoute(new ApiPath("/inventory/1")));
        assertNull(router.findRoute(new ApiPath("/inventory/1/locations")));
        assertNotNull(router.findRoute(new ApiPath("/orders/1")));
        assertNull(router.findRoute(new ApiPath("/orders/1/releases")));

        router = new StaticApiRouter(new ApiRoute[]{
            new ApiRoute(new ApiPath("/orders"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders/{id}"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders/{id}/releases"), testActorImage, emptyApiDesc),
            new ApiRoute(new ApiPath("/orders/{id}/releases/{id}"), testActorImage, emptyApiDesc)
        });
        assertNotNull(router.findRoute(new ApiPath("/orders")));
        assertNotNull(router.findRoute(new ApiPath("/orders/1")));
        assertNotNull(router.findRoute(new ApiPath("/orders/1/releases")));
        assertNotNull(router.findRoute(new ApiPath("/orders/1/releases/1")));
        assertNull(router.findRoute(new ApiPath("/shippers")));
        assertNull(router.findRoute(new ApiPath("/orders/1/customer")));
        assertNull(router.findRoute(new ApiPath("/orders/1/customer/address")));
    }

}
