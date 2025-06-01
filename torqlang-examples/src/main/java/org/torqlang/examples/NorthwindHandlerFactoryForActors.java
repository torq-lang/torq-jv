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
import org.torqlang.lang.*;
import org.torqlang.local.*;
import org.torqlang.server.ApiDesc;
import org.torqlang.server.ApiHandler;
import org.torqlang.server.ApiRouter;
import org.torqlang.server.RateLimiter;
import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.SourceFileBroker;
import org.torqlang.util.SourceString;

import java.util.List;

import static org.torqlang.examples.ExamplesSourceBroker.EXAMPLES_ROOT;
import static org.torqlang.examples.ExamplesSourceBroker.NORTHWIND;

public final class NorthwindHandlerFactoryForActors {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static Handler createApiHandler() throws Exception {

        SourceFileBroker sourceBroker = ExamplesSourceBroker.createResourcesBrokerForActors();

        // ------------
        // Actor System
        // ------------

        CompleteRec examplesMod = Rec.completeRecBuilder()
            .addField(NorthwindDbMod.NORTHWIND_DB_STR, NorthwindDbMod.singleton().namesake())
            .build();

        ActorSystem system = ActorSystem.builder()
            .addDefaultPackages()
            .addPackage("northwind", examplesMod)
            .build();

        // ---------
        // Customers
        // ---------

        ApiDesc customerApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON)))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc customersApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.CUSTOMER_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        SourceString customersHandlerSource = sourceBroker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "CustomersHandler.torq"))
        );
        ActorImage customersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(customersHandlerSource.content());

        // ---------
        // Employees
        // ---------

        ApiDesc employeeApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON)))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc employeesApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.EMPLOYEE_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        SourceString employeesHandlerSource = sourceBroker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "EmployeesHandler.torq"))
        );
        ActorImage employeesHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(employeesHandlerSource.content());

        // ------
        // Orders
        // ------

        ApiDesc orderApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON)))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc ordersApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.ORDER_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc orderDetailsApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON, StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.ORDER_DETAILS_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        SourceString ordersHandlerSource = sourceBroker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "OrdersHandler.torq"))
        );
        ActorImage ordersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(ordersHandlerSource.content());

        // --------
        // Products
        // --------

        ApiDesc productApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON)))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc productsApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.PRODUCT_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        SourceString productsHandlerSource = sourceBroker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "ProductsHandler.torq"))
        );
        ActorImage productsHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(productsHandlerSource.content());

        // ---------
        // Suppliers
        // ---------

        ApiDesc supplierApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int64Type.SINGLETON)))
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        ApiDesc suppliersApiDesc = ApiDesc.builder()
            .setPathType(TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON)))
            .setQueryType(NorthwindTypes.PRODUCT_TYPE)
            .setContextProvider(NorthwindHandlerFactoryForActors::emptyContextProvider)
            .build();
        SourceString suppliersHandlerSource = sourceBroker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "SuppliersHandler.torq"))
        );
        ActorImage suppliersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(suppliersHandlerSource.content());

        // -----------
        // API Handler
        // -----------

        /*
         * This API system is rate limited to a total of 12,000 transactions per second. Order list query has its own
         * rate limiter because its throughput is much lower than the others as it returns 48 records where each record
         * is joined to a customer and an employee for a total of 97 queries per request. The other API routes share
         * the remaining rate limit set to 10,000 transactions per second.
         */
        RateLimiter sharedTenThousandLimit = RateLimiter.create(10_000);
        RateLimiter ordersTwoThousandLimit = RateLimiter.create(2_000);

        return ApiHandler.builder()
            .setRouter(ApiRouter.staticBuilder()
                .addRoute("/customers", customersHandlerImage, customersApiDesc, sharedTenThousandLimit)
                .addRoute("/customers/{id}", customersHandlerImage, customerApiDesc, sharedTenThousandLimit)
                .addRoute("/employees", employeesHandlerImage, employeesApiDesc, sharedTenThousandLimit)
                .addRoute("/employees/{id}", employeesHandlerImage, employeeApiDesc, sharedTenThousandLimit)
                .addRoute("/orders", ordersHandlerImage, ordersApiDesc, ordersTwoThousandLimit)
                .addRoute("/orders/{id}", ordersHandlerImage, orderApiDesc, sharedTenThousandLimit)
                .addRoute("/orders/{id}/details", ordersHandlerImage, orderDetailsApiDesc, sharedTenThousandLimit)
                .addRoute("/products", productsHandlerImage, productsApiDesc, sharedTenThousandLimit)
                .addRoute("/products/{id}", productsHandlerImage, productApiDesc, sharedTenThousandLimit)
                .addRoute("/suppliers", suppliersHandlerImage, suppliersApiDesc, sharedTenThousandLimit)
                .addRoute("/suppliers/{id}", suppliersHandlerImage, supplierApiDesc, sharedTenThousandLimit)
                .build())
            .build();
    }

}
