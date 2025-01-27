/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.lang.JsonFormatter;
import org.torqlang.lang.JsonParser;

import java.io.*;
import java.util.*;

public final class NorthwindFiles {

    public static final String FILES_DIR = System.getProperty("user.home") + "/.torq_lang/resources/northwind";

    public static final List<String> CUSTOMERS_KEY_NAMES = List.of("id");
    public static final List<String> EMPLOYEE_PRIVILEGES_KEY_NAMES = List.of("employee_id", "privilege_id");
    public static final List<String> EMPLOYEES_KEY_NAMES = List.of("id");
    public static final List<String> INVENTORY_TRANSACTION_TYPES_KEY_NAMES = List.of("id");
    public static final List<String> INVENTORY_TRANSACTIONS_KEY_NAMES = List.of("id");
    public static final List<String> INVOICES_KEY_NAMES = List.of("id");
    public static final List<String> ORDER_DETAILS_KEY_NAMES = List.of("order_id", "line_seq");
    public static final List<String> ORDER_DETAILS_STATUS_KEY_NAMES = List.of("id");
    public static final List<String> ORDER_STATUS_KEY_NAMES = List.of("id");
    public static final List<String> ORDER_TAX_STATUS_KEY_NAMES = List.of("id");
    public static final List<String> ORDERS_KEY_NAMES = List.of("id");
    public static final List<String> PRIVILEGES_KEY_NAMES = List.of("id");
    public static final List<String> PRODUCT_SUPPLIERS_KEY_NAMES = List.of("product_id", "supplier_id");
    public static final List<String> PRODUCTS_KEY_NAMES = List.of("id");
    public static final List<String> PURCHASE_ORDER_DETAILS_KEY_NAMES = List.of("purchase_order_id", "line_seq");
    public static final List<String> PURCHASE_ORDER_STATUS_KEY_NAMES = List.of("id");
    public static final List<String> PURCHASE_ORDERS_KEY_NAMES = List.of("id");
    public static final List<String> SHIPPERS_KEY_NAMES = List.of("id");
    public static final List<String> SUPPLIERS_KEY_NAMES = List.of("id");

    public static final String CUSTOMERS_COLL_NAME = "customers";
    public static final String EMPLOYEE_PRIVILEGES_COLL_NAME = "employee_privileges";
    public static final String EMPLOYEES_COLL_NAME = "employees";
    public static final String INVENTORY_TRANSACTION_TYPES_COLL_NAME = "inventory_transaction_types";
    public static final String INVENTORY_TRANSACTIONS_COLL_NAME = "inventory_transactions";
    public static final String INVOICES_COLL_NAME = "invoices";
    public static final String ORDER_DETAILS_COLL_NAME = "order_details";
    public static final String ORDER_DETAILS_STATUS_COLL_NAME = "order_details_status";
    public static final String ORDER_STATUS_COLL_NAME = "order_status";
    public static final String ORDER_TAX_STATUS_COLL_NAME = "order_tax_status";
    public static final String ORDERS_COLL_NAME = "orders";
    public static final String PRIVILEGES_COLL_NAME = "privileges";
    public static final String PRODUCT_SUPPLIERS_COLL_NAME = "product_suppliers";
    public static final String PRODUCTS_COLL_NAME = "products";
    public static final String PURCHASE_ORDER_DETAILS_COLL_NAME = "purchase_order_details";
    public static final String PURCHASE_ORDER_STATUS_COLL_NAME = "purchase_order_status";
    public static final String PURCHASE_ORDERS_COLL_NAME = "purchase_orders";
    public static final String SHIPPERS_COLL_NAME = "shippers";
    public static final String SUPPLIERS_COLL_NAME = "suppliers";

    public static final Map<String, List<String>> KEY_NAMES_BY_COLL;

    static {
        Map<String, List<String>> map = new HashMap<>();
        map.put(CUSTOMERS_COLL_NAME, CUSTOMERS_KEY_NAMES);
        map.put(EMPLOYEE_PRIVILEGES_COLL_NAME, EMPLOYEE_PRIVILEGES_KEY_NAMES);
        map.put(EMPLOYEES_COLL_NAME, EMPLOYEES_KEY_NAMES);
        map.put(INVENTORY_TRANSACTION_TYPES_COLL_NAME, INVENTORY_TRANSACTION_TYPES_KEY_NAMES);
        map.put(INVENTORY_TRANSACTIONS_COLL_NAME, INVENTORY_TRANSACTIONS_KEY_NAMES);
        map.put(INVOICES_COLL_NAME, INVOICES_KEY_NAMES);
        map.put(ORDER_DETAILS_COLL_NAME, ORDER_DETAILS_KEY_NAMES);
        map.put(ORDER_DETAILS_STATUS_COLL_NAME, ORDER_DETAILS_STATUS_KEY_NAMES);
        map.put(ORDER_STATUS_COLL_NAME, ORDER_STATUS_KEY_NAMES);
        map.put(ORDER_TAX_STATUS_COLL_NAME, ORDER_TAX_STATUS_KEY_NAMES);
        map.put(ORDERS_COLL_NAME, ORDERS_KEY_NAMES);
        map.put(PRIVILEGES_COLL_NAME, PRIVILEGES_KEY_NAMES);
        map.put(PRODUCT_SUPPLIERS_COLL_NAME, PRODUCT_SUPPLIERS_KEY_NAMES);
        map.put(PRODUCTS_COLL_NAME, PRODUCTS_KEY_NAMES);
        map.put(PURCHASE_ORDER_DETAILS_COLL_NAME, PURCHASE_ORDER_DETAILS_KEY_NAMES);
        map.put(PURCHASE_ORDER_STATUS_COLL_NAME, PURCHASE_ORDER_STATUS_KEY_NAMES);
        map.put(PURCHASE_ORDERS_COLL_NAME, PURCHASE_ORDERS_KEY_NAMES);
        map.put(SHIPPERS_COLL_NAME, SHIPPERS_KEY_NAMES);
        map.put(SUPPLIERS_COLL_NAME, SUPPLIERS_KEY_NAMES);
        KEY_NAMES_BY_COLL = Map.copyOf(map);
    }

    public static boolean containsCriteria(Map<String, Object> rec, Map<String, Object> criteria) {
        for (String featName : criteria.keySet()) {
            if (rec.get(featName) == null) {
                return false;
            }
            Object recVal = rec.get(featName);
            Object criteriaVal = criteria.get(featName);
            if (recVal instanceof Number recNumber && criteriaVal instanceof Number criteriaNumber) {
                if (recNumber.doubleValue() != criteriaNumber.doubleValue()) {
                    return false;
                }
            } else {
                if (!(rec.get(featName).equals(criteria.get(featName)))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Map<String, Object> extractKey(Map<String, Object> rec, Collection<String> keyFields) {
        Map<String, Object> key = new HashMap<>();
        for (String keyName : keyFields) {
            key.put(keyName, rec.get(keyName));
        }
        return key;
    }

    public static NorthwindColl fetchColl(NorthwindCache cache,
                                          String directory,
                                          String collName)
        throws IOException
    {
        NorthwindColl coll = cache.data.get(collName);
        if (coll != null) {
            return coll;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        String filePath = directory + "/" + collName + ".nldjson";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> rec = JsonParser.parseAndCast(line);
                list.add(rec);
            }
        }
        coll = new NorthwindColl(collName, list);
        cache.data.put(collName, coll);
        return coll;
    }

    public static Map<String, Object> fetchRec(NorthwindCache cache,
                                               String directory,
                                               String collName,
                                               Map<String, Object> key)
        throws IOException
    {
        NorthwindColl coll = fetchColl(cache, directory, collName);
        return fetchRec(coll, key);
    }

    public static Map<String, Object> fetchRec(NorthwindColl coll,
                                               Map<String, Object> key)
    {
        for (Map<String, Object> rec : coll.list()) {
            if (containsCriteria(rec, key)) {
                return rec;
            }
        }
        return null;
    }

    public static NorthwindColl filterColl(NorthwindColl coll,
                                           Map<String, Object> criteria)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> rec : coll.list()) {
            if (containsCriteria(rec, criteria)) {
                list.add(rec);
            }
        }
        return new NorthwindColl(coll.name(), list);
    }

    public static void saveColl(NorthwindColl coll, String directory)
        throws IOException
    {
        String filePath = directory + "/" + coll.name() + ".nldjson";
        boolean first = true;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Map<String, Object> item : coll.list()) {
                if (!first) {
                    bw.newLine();
                }
                bw.write(JsonFormatter.DEFAULT.format(item));
                first = false;
            }
        }
    }

}
