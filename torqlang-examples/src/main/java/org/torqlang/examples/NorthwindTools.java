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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public final class NorthwindTools {

    public static final String ROOT_DIR = System.getProperty("user.home") + "/.torq_lang/northwind";

    public static final String NORTHWIND_CUSTOMERS_JSON = "/northwind/customers.json";
    public static final String NORTHWIND_EMPLOYEES_JSON = "/northwind/employees.json";
    public static final String NORTHWIND_INVENTORY_TRANSACTION_TYPES_JSON = "/northwind/inventory_transaction_types.json";
    public static final String NORTHWIND_INVENTORY_TRANSACTIONS_JSON = "/northwind/inventory_transactions.json";
    public static final String NORTHWIND_INVOICES_JSON = "/northwind/invoices.json";
    public static final String NORTHWIND_ORDER_DETAILS_JSON = "/northwind/order_details.json";
    public static final String NORTHWIND_ORDER_DETAILS_STATUS_JSON = "/northwind/order_details_status.json";
    public static final String NORTHWIND_ORDER_STATUS_JSON = "/northwind/order_status.json";
    public static final String NORTHWIND_ORDER_TAX_STATUS_JSON = "/northwind/order_tax_status.json";
    public static final String NORTHWIND_ORDERS_JSON = "/northwind/orders.json";
    public static final String NORTHWIND_PRIVILEGES_JSON = "/northwind/privileges.json";
    public static final String NORTHWIND_PRODUCTS_JSON = "/northwind/products.json";
    public static final String NORTHWIND_PURCHASE_ORDER_DETAILS_JSON = "/northwind/purchase_order_details.json";
    public static final String NORTHWIND_PURCHASE_ORDER_STATUS_JSON = "/northwind/purchase_order_status.json";
    public static final String NORTHWIND_PURCHASE_ORDERS_JSON = "/northwind/purchase_orders.json";
    public static final String NORTHWIND_SHIPPERS_JSON = "/northwind/shippers.json";
    public static final String NORTHWIND_SUPPLIERS_JSON = "/northwind/suppliers.json";

    private static final ConcurrentHashMap<String, String> jsonTextCache = new ConcurrentHashMap<>();

    public static String fetchJsonText(String filePath) throws Exception {
        String jsonText = jsonTextCache.get(filePath);
        if (jsonText == null) {
            jsonText = readTextFromResource(filePath);
            jsonTextCache.put(filePath, jsonText);
        }
        return jsonText;
    }

    public static void convertToNldjson(String filePath) throws Exception {
        System.out.println("============ " + filePath + " ============");
        System.out.println(listToNldJson(jsonTextToNativeList(fetchJsonText(filePath))));
    }

    public static void main(String[] args) throws Exception {
        convertToNldjson(NORTHWIND_EMPLOYEES_JSON);
        convertToNldjson(NORTHWIND_CUSTOMERS_JSON);
        convertToNldjson(NORTHWIND_INVENTORY_TRANSACTION_TYPES_JSON);
        convertToNldjson(NORTHWIND_INVENTORY_TRANSACTIONS_JSON);
        convertToNldjson(NORTHWIND_INVOICES_JSON);
        convertToNldjson(NORTHWIND_ORDERS_JSON);
        convertToNldjson(NORTHWIND_ORDER_DETAILS_JSON);
        convertToNldjson(NORTHWIND_ORDER_DETAILS_STATUS_JSON);
        convertToNldjson(NORTHWIND_ORDER_STATUS_JSON);
        convertToNldjson(NORTHWIND_ORDER_TAX_STATUS_JSON);
        convertToNldjson(NORTHWIND_PRIVILEGES_JSON);
        convertToNldjson(NORTHWIND_PRODUCTS_JSON);
        convertToNldjson(NORTHWIND_PURCHASE_ORDER_DETAILS_JSON);
        convertToNldjson(NORTHWIND_PURCHASE_ORDER_STATUS_JSON);
        convertToNldjson(NORTHWIND_PURCHASE_ORDERS_JSON);
        convertToNldjson(NORTHWIND_SHIPPERS_JSON);
        convertToNldjson(NORTHWIND_SUPPLIERS_JSON);
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

    public static List<Map<String, Object>> fetchColl(Map<String, List<Map<String, Object>>> cache,
                                                      String directory, String collName)
        throws IOException
    {
        List<Map<String, Object>> coll = cache.get(collName);
        if (coll != null) {
            return coll;
        }
        coll = new ArrayList<>();
        String filePath = directory + "/" + collName + ".nldjson";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                coll.add(JsonParser.parseAndCast(line));
            }
        }
        cache.put(collName, coll);
        return coll;
    }

    public static Map<String, Object> fetchRec(Map<String, List<Map<String, Object>>> cache,
                                               String directory, String collName, long id)
        throws IOException
    {
        List<Map<String, Object>> coll = fetchColl(cache, directory, collName);
        Map<String, Object> rec = null;
        for (Map<String, Object> item : coll) {
            long itemId = (long) item.get("id");
            if (itemId == id) {
                rec = item;
                break;
            }
        }
        return rec;
    }

    public static List<Object> jsonTextToNativeList(String jsonText) {
        return (List<Object>) JsonParser.parse(jsonText);
    }

    public static String listToNldJson(List<Object> list) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Object obj : list) {
            Map<String, Object> map = (Map<String, Object>) obj;
            String s = JsonFormatter.SINGLETON.format(map);
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
        URL url = NorthwindTools.class.getResource(absolutePath);
        if (url == null) {
            throw new FileNotFoundException(absolutePath);
        }
        try (InputStream s = url.openStream()) {
            return new String(s.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
