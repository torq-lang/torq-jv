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
import org.torqlang.util.FileBroker;
import org.torqlang.util.FileName;
import org.torqlang.util.FileType;

import static org.torqlang.examples.ExamplesSourceBroker.EXAMPLES_ROOT;
import static org.torqlang.examples.ExamplesSourceBroker.NORTHWIND;

public final class NorthwindHandlerFactoryForActors {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static Handler createApiHandler() throws Exception {

        FileBroker sourceBroker = ExamplesSourceBroker.createResourcesBrokerForActors();

        CompleteRec examplesMod = Rec.completeRecBuilder()
            .addField(Str.of("NorthwindDb"), NorthwindDbPack.NORTHWIND_DB_ACTOR)
            .build();

        ActorSystem system = ActorSystem.builder()
            .addDefaultModules()
            .addModule("examples", examplesMod)
            .build();

        ApiDesc customerApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc customersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.CUSTOMER_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        String customersHandlerSource = sourceBroker.source(
            FileBroker.append(FileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.TORQ, "CustomersHandler.torq"))
        );
        ActorImage customersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(customersHandlerSource);

        ApiDesc employeeApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc employeesApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.EMPLOYEE_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        String employeesHandlerSource = sourceBroker.source(
            FileBroker.append(FileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.TORQ, "EmployeesHandler.torq"))
        );
        ActorImage employeesHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(employeesHandlerSource);

        ApiDesc orderApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc ordersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc orderDetailsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC, StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.ORDER_DETAILS_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        String ordersHandlerSource = sourceBroker.source(
            FileBroker.append(FileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.TORQ, "OrdersHandler.torq"))
        );
        ActorImage ordersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(ordersHandlerSource);

        ApiDesc productApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc productsApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        String productsHandlerSource = sourceBroker.source(
            FileBroker.append(FileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.TORQ, "ProductsHandler.torq"))
        );
        ActorImage productsHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(productsHandlerSource);

        ApiDesc supplierApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC, Int64Desc.BASIC))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc suppliersApiDesc = ApiDesc.builder()
            .setPathDesc(TupleDesc.of(StrDesc.BASIC))
            .setQueryDesc(NorthwindDescs.PRODUCT_DESC)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        String suppliersHandlerSource = sourceBroker.source(
            FileBroker.append(FileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.TORQ, "SuppliersHandler.torq"))
        );
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
