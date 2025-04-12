/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.lang.JsonFormatter;
import org.torqlang.lang.JsonParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NorthwindJson {

    public static final String DATA_DIR = "/northwind/data/";
    public static final String TORQSRC_DIR = "/northwind/torqsrc/examples/";
    public static final String SUPPLIERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.SUPPLIERS_COLL_NAME + ".json";
    public static final String SHIPPERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.SHIPPERS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PURCHASE_ORDERS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDER_STATUS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PURCHASE_ORDER_STATUS_COLL_NAME + ".json";
    public static final String PURCHASE_ORDER_DETAILS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PURCHASE_ORDER_DETAILS_COLL_NAME + ".json";
    public static final String PRODUCT_SUPPLIERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PRODUCT_SUPPLIERS_COLL_NAME + ".json";
    public static final String PRODUCTS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PRODUCTS_COLL_NAME + ".json";
    public static final String PRIVILEGES_JSON_RESOURCE = DATA_DIR + NorthwindFiles.PRIVILEGES_COLL_NAME + ".json";
    public static final String ORDERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.ORDERS_COLL_NAME + ".json";
    public static final String ORDER_TAX_STATUS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.ORDER_TAX_STATUS_COLL_NAME + ".json";
    public static final String ORDER_STATUS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.ORDER_STATUS_COLL_NAME + ".json";
    public static final String ORDER_DETAILS_STATUS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.ORDER_DETAILS_STATUS_COLL_NAME + ".json";
    public static final String ORDER_DETAILS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.ORDER_DETAILS_COLL_NAME + ".json";
    public static final String INVOICES_JSON_RESOURCE = DATA_DIR + NorthwindFiles.INVOICES_COLL_NAME + ".json";
    public static final String INVENTORY_TRANSACTIONS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.INVENTORY_TRANSACTIONS_COLL_NAME + ".json";
    public static final String INVENTORY_TRANSACTION_TYPES_JSON_RESOURCE = DATA_DIR + NorthwindFiles.INVENTORY_TRANSACTION_TYPES_COLL_NAME + ".json";
    public static final String EMPLOYEE_PRIVILEGES_JSON_RESOURCE = DATA_DIR + NorthwindFiles.EMPLOYEE_PRIVILEGES_COLL_NAME + ".json";
    public static final String EMPLOYEES_JSON_RESOURCE = DATA_DIR + NorthwindFiles.EMPLOYEES_COLL_NAME + ".json";
    public static final String CUSTOMERS_JSON_RESOURCE = DATA_DIR + NorthwindFiles.CUSTOMERS_COLL_NAME + ".json";

    static final Map<String, String> jsonTextCache = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        convertToNldjson(NorthwindJson.EMPLOYEE_PRIVILEGES_JSON_RESOURCE);
        convertToNldjson(NorthwindJson.PRODUCT_SUPPLIERS_JSON_RESOURCE);
    }

    public static void convertToNldjson(String filePath) throws Exception {
        System.out.println("============ " + filePath + " ============");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) JsonParser.parse(fetchJsonText(filePath));
        System.out.println(listToNldJson(list));
    }

    private static void convertAllToNldJson() throws Exception {
        convertToNldjson(EMPLOYEE_PRIVILEGES_JSON_RESOURCE);
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
        convertToNldjson(PRODUCT_SUPPLIERS_JSON_RESOURCE);
        convertToNldjson(PRODUCTS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDER_DETAILS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDER_STATUS_JSON_RESOURCE);
        convertToNldjson(PURCHASE_ORDERS_JSON_RESOURCE);
        convertToNldjson(SHIPPERS_JSON_RESOURCE);
        convertToNldjson(SUPPLIERS_JSON_RESOURCE);
    }

    public static String fetchJsonText(String filePath) throws Exception {
        String jsonText = jsonTextCache.get(filePath);
        if (jsonText == null) {
            jsonText = readTextFromResource(filePath);
            jsonTextCache.put(filePath, jsonText);
        }
        return jsonText;
    }

    public static String listToNldJson(List<Map<String, Object>> list) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map<String, Object> obj : list) {
            String s = JsonFormatter.DEFAULT.format(obj);
            if (!first) {
                buf.append("\n");
            }
            buf.append(s);
            first = false;
        }
        return buf.toString();
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
}
