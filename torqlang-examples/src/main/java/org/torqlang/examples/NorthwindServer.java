/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.eclipse.jetty.server.Request;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Rec;
import org.torqlang.local.Actor;
import org.torqlang.local.ActorImage;
import org.torqlang.local.ApiRouter;
import org.torqlang.server.ApiHandler;
import org.torqlang.server.CoreServer;
import org.torqlang.server.EchoHandler;

public final class NorthwindServer {

    public static CompleteRec contextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static void main(String[] args) throws Exception {

        String queryOrdersSource = QueryOrders.SOURCE.replace("${1}",
            NorthwindFiles.fetchJsonText(NorthwindFiles.ORDERS_JSON_RESOURCE));

        ActorImage ordersImage = Actor.captureImage(queryOrdersSource);

        CoreServer server = CoreServer.builder()
            .setPort(8080)
            .addContextHandler(new EchoHandler(), "/echo")
            .addContextHandler(ApiHandler.builder()
                .setApiRouter(ApiRouter.staticBuilder()
                    .addRoute("/orders", ordersImage)
                    .addRoute("/orders/{id}", ordersImage)
                    .build())
                .setContextProvider(NorthwindServer::contextProvider)
                .build(), "/api")
            .build();
        server.start();
        server.join();
    }

}
