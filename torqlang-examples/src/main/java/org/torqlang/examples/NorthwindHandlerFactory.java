/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.local.*;
import org.torqlang.server.ApiDesc;
import org.torqlang.server.ApiHandler;
import org.torqlang.server.ApiRouter;
import org.torqlang.util.ReadTextFromResource;

import java.lang.invoke.MethodHandles;

public final class NorthwindHandlerFactory {

    public static final String TORQSRC_DIR = "/org/torqlang/examples/torqsrc/northwind/";

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static Handler createApiHandler() throws Exception {

        CompleteRec examplesMod = Rec.completeRecBuilder()
            .addField(Str.of("NorthwindDb"), NorthwindDbPack.NORTHWIND_DB_ACTOR)
            .build();

        ActorSystem system = ActorSystem.builder()
            .addDefaultModules()
            .addModule("examples", examplesMod)
            .build();

        ApiDesc customerApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc customersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.CUSTOMER_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        String customersHandlerSource = ReadTextFromResource.apply(MethodHandles.lookup().lookupClass(), TORQSRC_DIR + "CustomersHandler.torq");
        ActorImage customersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(customersHandlerSource);

        ApiDesc employeeApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc employeesApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.EMPLOYEE_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        String employeesHandlerSource = ReadTextFromResource.apply(MethodHandles.lookup().lookupClass(), TORQSRC_DIR + "EmployeesHandler.torq");
        ActorImage employeesHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(employeesHandlerSource);

        ApiDesc orderApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc ordersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc orderDetailsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC, StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DETAILS_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        String ordersHandlerSource = ReadTextFromResource.apply(MethodHandles.lookup().lookupClass(), TORQSRC_DIR + "OrdersHandler.torq");
        ActorImage ordersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(ordersHandlerSource);

        ApiDesc productApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc productsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        String productsHandlerSource = ReadTextFromResource.apply(MethodHandles.lookup().lookupClass(), TORQSRC_DIR + "ProductsHandler.torq");
        ActorImage productsHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(productsHandlerSource);

        ApiDesc supplierApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        ApiDesc suppliersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindHandlerFactory::emptyContextProvider)
            .build();
        String suppliersHandlerSource = ReadTextFromResource.apply(MethodHandles.lookup().lookupClass(), TORQSRC_DIR + "SuppliersHandler.torq");
        ActorImage suppliersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(suppliersHandlerSource);

        return ApiHandler.builder()
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
            .build();
    }

}
