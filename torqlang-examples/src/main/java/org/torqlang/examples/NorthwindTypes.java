/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.lang.RecType;

import java.util.HashMap;
import java.util.Map;

public final class NorthwindTypes {

    public static final RecType CUSTOMER_TYPE = RecType.SINGLETON;

    public static final RecType EMPLOYEE_TYPE = RecType.SINGLETON;

    public static final RecType ORDER_TYPE = RecType.SINGLETON;

    public static final RecType ORDER_DETAILS_TYPE = RecType.SINGLETON;

    public static final RecType PRODUCT_TYPE = RecType.SINGLETON;

    public static final RecType SUPPLIER_TYPE = RecType.SINGLETON;

    public static final Map<String, RecType> NORTHWIND_TYPES_BY_COLL_NAME;

    static {
        HashMap<String, RecType> map = new HashMap<>();
        map.put(NorthwindFiles.CUSTOMERS_COLL_NAME, CUSTOMER_TYPE);
        map.put(NorthwindFiles.EMPLOYEES_COLL_NAME, EMPLOYEE_TYPE);
        map.put(NorthwindFiles.ORDERS_COLL_NAME, ORDER_TYPE);
        map.put(NorthwindFiles.ORDER_DETAILS_COLL_NAME, ORDER_DETAILS_TYPE);
        map.put(NorthwindFiles.PRODUCTS_COLL_NAME, PRODUCT_TYPE);
        map.put(NorthwindFiles.SHIPPERS_COLL_NAME, SUPPLIER_TYPE);
        NORTHWIND_TYPES_BY_COLL_NAME = Map.copyOf(map);
    }
}
