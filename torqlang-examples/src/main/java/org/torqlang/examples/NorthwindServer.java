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
import org.torqlang.klvm.Str;
import org.torqlang.local.*;
import org.torqlang.server.*;

/*
 * Example data:
 *     Example data must be copied from the project directory `resources/northwind/` to the local home
 *     directory `/home/USER/.torq_lang/northwind`.
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
 * Run with 8 hardware threads:
 *     taskset -c 0-7 java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
 */
public final class NorthwindServer {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static void main(String[] args) throws Exception {

        ConsoleLogger.SINGLETON.info("Process ID: " + ProcessHandle.current().pid());
        ConsoleLogger.SINGLETON.info("System executor: " + ActorSystem.defaultExecutor());
        ConsoleLogger.SINGLETON.info("NorthwindDb executor: " + NorthwindDbPack.NORTHWIND_DB_EXECUTOR);

        CompleteRec examplesMod = Rec.completeRecBuilder()
            .addField(Str.of("NorthwindDb"), NorthwindDbPack.NORTHWIND_DB_ACTOR)
            .build();
        ActorSystem system = ActorSystem.builder()
            .addDefaultModules()
            .addModule("examples", examplesMod)
            .build();

        ApiDesc customerApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc customersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.CUSTOMER_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        String customersHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.RESOURCES_DIR + "CustomersHandler.torq");
        ActorImage customersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(customersHandlerSource);

        ApiDesc employeeApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc employeesApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.EMPLOYEE_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        String employeesHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.RESOURCES_DIR + "EmployeesHandler.torq");
        ActorImage employeesHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(employeesHandlerSource);

        ApiDesc orderApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc ordersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc orderDetailsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC, StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DETAILS_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        String ordersHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.RESOURCES_DIR + "OrdersHandler.torq");
        ActorImage ordersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(ordersHandlerSource);

        ApiDesc productApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc productsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        String productsHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.RESOURCES_DIR + "ProductsHandler.torq");
        ActorImage productsHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(productsHandlerSource);

        ApiDesc supplierApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        ApiDesc suppliersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindServer::emptyContextProvider)
            .build();
        String suppliersHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.RESOURCES_DIR + "SuppliersHandler.torq");
        ActorImage suppliersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(suppliersHandlerSource);

        LocalServer server = LocalServer.builder()
            .setPort(8080)
            .addContextHandler(new EchoHandler(), "/echo")
            .addContextHandler(ApiHandler.builder()
                .setRouter(ApiRouter.staticBuilder()
                    .addRoute("/customers", customersHandlerImage, customersApiDesc)
                    .addRoute("/customers/{id}", customersHandlerImage, customerApiDesc)
                    .addRoute("/employees", employeesHandlerImage, employeesApiDesc)
                    .addRoute("/employees/{id}", employeesHandlerImage, employeeApiDesc)
                    .addRoute("/orders", ordersHandlerImage, ordersApiDesc)
                    .addRoute("/orders/{id}", ordersHandlerImage, orderApiDesc)
                    .addRoute("/orders/{id}/details", ordersHandlerImage, orderDetailsApiDesc)
                    .addRoute("/products", productsHandlerImage, productsApiDesc)
                    .addRoute("/products/{id}", productsHandlerImage, productApiDesc)
                    .addRoute("/suppliers", suppliersHandlerImage, suppliersApiDesc)
                    .addRoute("/suppliers/{id}", suppliersHandlerImage, supplierApiDesc)
                    .build())
                .build(), "/api")
            .build();
        server.start();
        server.join();
    }

}
