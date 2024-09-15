/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.FailedValue;
import org.torqlang.lang.JsonFormatter;
import org.torqlang.lang.JsonParser;
import org.torqlang.local.Envelope;
import org.torqlang.local.FutureResponse;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public final class NorthwindFiles {

    public static final String FILES_DIR = System.getProperty("user.home") + "/.torq_lang/northwind";
    public static final String RESOURCES_DIR = "/northwind/";

    public static final List<String> CUSTOMERS_KEY_NAMES = List.of("id");
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
    public static final List<String> PRODUCTS_KEY_NAMES = List.of("id");
    public static final List<String> PURCHASE_ORDER_DETAILS_KEY_NAMES = List.of("purchase_order_id", "line_seq");
    public static final List<String> PURCHASE_ORDER_STATUS_KEY_NAMES = List.of("id");
    public static final List<String> PURCHASE_ORDERS_KEY_NAMES = List.of("id");
    public static final List<String> SHIPPERS_KEY_NAMES = List.of("id");
    public static final List<String> SUPPLIERS_KEY_NAMES = List.of("id");

    public static final String CUSTOMERS_COLL_NAME = "customers";
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
        map.put(PRODUCTS_COLL_NAME, PRODUCTS_KEY_NAMES);
        map.put(PURCHASE_ORDER_DETAILS_COLL_NAME, PURCHASE_ORDER_DETAILS_KEY_NAMES);
        map.put(PURCHASE_ORDER_STATUS_COLL_NAME, PURCHASE_ORDER_STATUS_KEY_NAMES);
        map.put(PURCHASE_ORDERS_COLL_NAME, PURCHASE_ORDERS_KEY_NAMES);
        map.put(SHIPPERS_COLL_NAME, SHIPPERS_KEY_NAMES);
        map.put(SUPPLIERS_COLL_NAME, SUPPLIERS_KEY_NAMES);
        KEY_NAMES_BY_COLL = Map.copyOf(map);
    }

    public static final String CUSTOMERS_JSON_RESOURCE = RESOURCES_DIR + CUSTOMERS_COLL_NAME + ".json";
    public static final String EMPLOYEES_JSON_RESOURCE = RESOURCES_DIR + EMPLOYEES_COLL_NAME + ".json";
    public static final String INVENTORY_TRANSACTION_TYPES_JSON_RESOURCE = RESOURCES_DIR + INVENTORY_TRANSACTION_TYPES_COLL_NAME + ".json";
    public static final String INVENTORY_TRANSACTIONS_JSON_RESOURCE = RESOURCES_DIR + INVENTORY_TRANSACTIONS_COLL_NAME + ".json";
    public static final String INVOICES_JSON_RESOURCE = RESOURCES_DIR + INVOICES_COLL_NAME + ".json";
    public static final String ORDER_DETAILS_JSON_RESOURCE = RESOURCES_DIR + ORDER_DETAILS_COLL_NAME + ".json";
    public static final String ORDER_DETAILS_STATUS_JSON_RESOURCE = RESOURCES_DIR + ORDER_DETAILS_STATUS_COLL_NAME + ".json";
    public static final String ORDER_STATUS_JSON_RESOURCE = RESOURCES_DIR + ORDER_STATUS_COLL_NAME + ".json";
    public static final String ORDER_TAX_STATUS_JSON_RESOURCE = RESOURCES_DIR + ORDER_TAX_STATUS_COLL_NAME + ".json";
    public static final String ORDERS_JSON_RESOURCE = RESOURCES_DIR + ORDERS_COLL_NAME + ".json";
    public static final String PRIVILEGES_JSON_RESOURCE = RESOURCES_DIR + PRIVILEGES_COLL_NAME + ".json";
    public static final String PRODUCTS_JSON_RESOURCE = RESOURCES_DIR + PRODUCTS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDER_DETAILS_JSON_RESOURCE = RESOURCES_DIR + PURCHASE_ORDER_DETAILS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDER_STATUS_JSON_RESOURCE = RESOURCES_DIR + PURCHASE_ORDER_STATUS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDERS_JSON_RESOURCE = RESOURCES_DIR + PURCHASE_ORDERS_COLL_NAME + ".json";
    public static final String SHIPPERS_JSON_RESOURCE = RESOURCES_DIR + SHIPPERS_COLL_NAME + ".json";
    public static final String SUPPLIERS_JSON_RESOURCE = RESOURCES_DIR + SUPPLIERS_COLL_NAME + ".json";

    private static final ConcurrentHashMap<String, String> jsonTextCache = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        convertToNldjson(ORDERS_JSON_RESOURCE);
    }

    public static void checkResponse(int expectedId, FutureResponse futureResponse) throws Exception {
        Envelope resp = futureResponse.future().get(1, TimeUnit.SECONDS);
        if (resp.message() instanceof FailedValue) {
            throw new IllegalStateException("Request failed");
        }
        Map<String, Object> rec = (Map<String, Object>) resp.message();
        long id = (Long) rec.get("id");
        if (id != expectedId) {
            throw new IllegalStateException("id is not " + expectedId);
        }
    }

    public static boolean containsKey(Map<String, Object> rec, Map<String, Object> key) {
        for (String keyName : key.keySet()) {
            if (rec.get(keyName) != null && rec.get(keyName).equals(key.get(keyName))) {
                return true;
            }
        }
        return false;
    }

    public static void convertToNldjson(String filePath) throws Exception {
        System.out.println("============ " + filePath + " ============");
        List<Map<String, Object>> list = (List<Map<String, Object>>) JsonParser.parse(fetchJsonText(filePath));
        System.out.println(listToNldJson(list));
    }

    private static void convertAllToNldJson() throws Exception {
        convertToNldjson(EMPLOYEES_JSON_RESOURCE);
        convertToNldjson(CUSTOMERS_JSON_RESOURCE);
        convertToNldjson(INVENTORY_TRANSACTION_TYPES_JSON_RESOURCE);
        convertToNldjson(INVENTORY_TRANSACTIONS_JSON_RESOURCE);
        convertToNldjson(INVOICES_JSON_RESOURCE);
        convertToNldjson(ORDERS_JSON_RESOURCE);
        convertToNldjson(ORDER_DETAILS_JSON_RESOURCE);
        convertToNldjson(ORDER_DETAILS_STATUS_JSON_RESOURCE);
        convertToNldjson(ORDER_STATUS_JSON_RESOURCE);
        convertToNldjson(ORDER_TAX_STATUS_JSON_RESOURCE);
        convertToNldjson(PRIVILEGES_JSON_RESOURCE);
        convertToNldjson(PRODUCTS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDER_DETAILS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDER_STATUS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDERS_JSON_RESOURCE);
        convertToNldjson(SHIPPERS_JSON_RESOURCE);
        convertToNldjson(SUPPLIERS_JSON_RESOURCE);
    }

    public static Map<String, Object> extractKey(Map<String, Object> rec, Collection<String> keyFields) {
        Map<String, Object> key = new HashMap<>();
        for (String keyName : keyFields) {
            key.put(keyName, rec.get(keyName));
        }
        return key;
    }

    public static NorthwindColl fetchColl(NorthwindCache cache, String directory, String collName)
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

    public static String fetchJsonText(String filePath) throws Exception {
        String jsonText = jsonTextCache.get(filePath);
        if (jsonText == null) {
            jsonText = readTextFromResource(filePath);
            jsonTextCache.put(filePath, jsonText);
        }
        return jsonText;
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
        throws IOException
    {
        for (Map<String, Object> rec : coll.list()) {
            if (containsKey(rec, key)) {
                return rec;
            }
        }
        return null;
    }

    public static String listToNldJson(List<Map<String, Object>> list) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map<String, Object> obj : list) {
            String s = JsonFormatter.SINGLETON.format(obj);
            if (!first) {
                buf.append("\n");
            }
            buf.append(s);
            first = false;
        }
        return buf.toString();
    }

    public static void printTimingResults(String owner, long start, long stop, int readCount) {
        long totalTimeMillis = stop - start;
        System.out.println(owner);
        System.out.printf("  Total time: %,d millis\n", totalTimeMillis);
        System.out.printf("  Total reads: %,d\n", readCount);
        System.out.printf("  Millis per read: %,.5f\n", ((double) totalTimeMillis / readCount));
        double readsPerSecond = 1_000.0 / totalTimeMillis;
        System.out.printf("  Reads per second: %,.2f\n", (readsPerSecond * readCount));
    }

    public static String readTextFromResource(String absolutePath) throws IOException {
        URL url = NorthwindFiles.class.getResource(absolutePath);
        if (url == null) {
            throw new FileNotFoundException(absolutePath);
        }
        try (InputStream s = url.openStream()) {
            return new String(s.readAllBytes(), StandardCharsets.UTF_8);
        }
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
                bw.write(JsonFormatter.SINGLETON.format(item));
                first = false;
            }
        }
    }

}
