/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class TestParserModule {

    /*
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
        String ordersHandlerSource = NorthwindJson.readTextFromResource(
            NorthwindJson.TORQ_DIR + "OrdersHandler.torq");
        ActorImage ordersHandlerImage = Actor.builder()
            .setSystem(system)
            .actorImage(ordersHandlerSource);

     */

    /*
        return ApiHandler.builder()
            .setRouter(ApiRouter.staticBuilder()
                .addRoute("/orders", ordersHandlerImage, ordersApiDesc)
                .addRoute("/orders/{id}", ordersHandlerImage, orderApiDesc)
                .addRoute("/orders/{id}/details", ordersHandlerImage, orderDetailsApiDesc)
                .build())
            .build();
     */
    /*
            actor OrdersHandler() in
                import system.{ArrayList, Int64, LocalDate, Rec, ValueIter}
                import examples.NorthwindDb
                handle ask 'GET'#{'headers': headers, 'path': ['orders'], 'query': query, 'context': context} in
                    // handle GET /orders
                end
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'context': context} in
                    // handle GET /orders/{order_id}
                end
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64, 'details'], 'query': query, 'context': context} in
                    // handle GET /orders/{order_id}/details
                end
                handle ask 'PATCH'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'body': body, 'context': context} in
                    // handle PATCH /orders/{order_id}
                end
            end
     */


    @Test
    public void test01() throws Exception {
        String source = """
            package examples

            meta#{'export': true}
            type Order = {
                'id': Int64,
                'employee_id': Int64,
                'customer_id': Int64,
                'order_date': Date,
                'shipped_date': Date,
                'shipper_id': Int64,
                'ship_name': Str,
                'ship_address': Str,
                'ship_city': Str,
                'ship_state_province': Str,
                'ship_zip_postal_code': Int64,
                'ship_country_region': Str,
                'shipping_fee': Dec128,
                'taxes': Dec128,
                'payment_type': Str,
                'paid_date': Date,
                'notes': Str,
                'tax_rate': Flt64,
                'tax_status_id': Int32,
                'status_id': Int32
            }

            meta#{'export': true}
            type OrderDetails = {
                'order_id': Int64,
                'product_id': Int64,
                'line_seq': Int64,
                'quantity': Dec128,
                'unit_price': Dec128,
                'discount': Flt64,
                'status_id': Int64,
                'date_allocated': Date,
                'purchase_order_id': Int64,
                'inventory_id': Int64,
            }

            meta#{'export': true, 'stereotype': 'api-handler'}
            actor OrdersHandler() in
                import system.{ArrayList, Int64, LocalDate, Rec, ValueIter}
                import examples.NorthwindDb

                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders'], 'query': query, 'context': context} -> Array[Order] | Message in
                    skip
                end

                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'context': context} -> Order | Message in
                    skip
                end

                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64, 'details'], 'query': query, 'context': context} -> OrderDetails | Message in
                    skip
                end

                meta#{'export': true}
                handle ask 'PATCH'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'body': body::Order, 'context': context} -> Bool | Message in
                    skip
                end
            end""";
        String expectedFormat = """
            package examples
            meta#{'export': true}
            type Order = {'customer_id': Int64, 'employee_id': Int64, 'id': Int64, 'notes': Str, 'order_date': Date, 'paid_date': Date, 'payment_type': Str, 'ship_address': Str, 'ship_city': Str, 'ship_country_region': Str, 'ship_name': Str, 'ship_state_province': Str, 'ship_zip_postal_code': Int64, 'shipped_date': Date, 'shipper_id': Int64, 'shipping_fee': Dec128, 'status_id': Int32, 'tax_rate': Flt64, 'tax_status_id': Int32, 'taxes': Dec128}
            meta#{'export': true}
            type OrderDetails = {'date_allocated': Date, 'discount': Flt64, 'inventory_id': Int64, 'line_seq': Int64, 'order_id': Int64, 'product_id': Int64, 'purchase_order_id': Int64, 'quantity': Dec128, 'status_id': Int64, 'unit_price': Dec128}
            meta#{'export': true, 'stereotype': 'api-handler'}
            actor OrdersHandler() in
                import system.{ArrayList, Int64, LocalDate, Rec, ValueIter}
                import examples.NorthwindDb
                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders'], 'query': query, 'context': context} -> Array[Order] | Message in
                    skip
                end
                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'context': context} -> Order | Message in
                    skip
                end
                meta#{'export': true}
                handle ask 'GET'#{'headers': headers, 'path': ['orders', order_id::Int64, 'details'], 'query': query, 'context': context} -> OrderDetails | Message in
                    skip
                end
                meta#{'export': true}
                handle ask 'PATCH'#{'headers': headers, 'path': ['orders', order_id::Int64], 'query': query, 'body': body::Order, 'context': context} -> Bool | Message in
                    skip
                end
            end""";
        Parser p = new Parser(source);
        ModuleStmt mod;
        try {
            mod = p.parseModule();
            String actualFormat = mod.toString();
            assertEquals(expectedFormat, actualFormat);
            assertEquals(1, mod.packageStmt.path.size());
            assertEquals("examples", mod.packageStmt.path.get(0).ident.name);
            assertEquals(3, mod.body.size());
            assertInstanceOf(TypeStmt.class, mod.body.get(0));
            TypeStmt typeStmt = (TypeStmt) mod.body.get(0);
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            assertInstanceOf(TypeStmt.class, mod.body.get(1));
            typeStmt = (TypeStmt) mod.body.get(1);
            assertInstanceOf(MetaRec.class, typeStmt.metaStruct());
            assertInstanceOf(ActorStmt.class, mod.body.get(2));
            ActorStmt actorStmt = (ActorStmt) mod.body.get(2);
            assertInstanceOf(MetaRec.class, actorStmt.metaStruct());
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 50, 50);
        }
    }

}
