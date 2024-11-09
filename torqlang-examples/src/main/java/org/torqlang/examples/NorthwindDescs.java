/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.Str;
import org.torqlang.local.*;

import java.util.HashMap;
import java.util.Map;

public final class NorthwindDescs {

    public static final RecDesc CUSTOMER_DESC = RecDesc.builder()
        .add(Str.of("id"), Int64Desc.BASIC)
        .add(Str.of("company"), StrDesc.BASIC)
        .add(Str.of("last_name"), StrDesc.BASIC)
        .add(Str.of("first_name"), StrDesc.BASIC)
        .add(Str.of("email_address"), StrDesc.BASIC)
        .add(Str.of("job_title"), StrDesc.BASIC)
        .add(Str.of("business_phone"), StrDesc.BASIC)
        .add(Str.of("home_phone"), StrDesc.BASIC)
        .add(Str.of("mobile_phone"), StrDesc.BASIC)
        .add(Str.of("fax_number"), StrDesc.BASIC)
        .add(Str.of("address"), StrDesc.BASIC)
        .add(Str.of("city"), StrDesc.BASIC)
        .add(Str.of("state_province"), StrDesc.BASIC)
        .add(Str.of("zip_postal_code"), StrDesc.BASIC)
        .add(Str.of("country_region"), StrDesc.BASIC)
        .add(Str.of("web_page"), StrDesc.BASIC)
        .add(Str.of("notes"), StrDesc.BASIC)
        .add(Str.of("attachments"), StrDesc.BASIC)
        .build();

    public static final RecDesc EMPLOYEE_DESC = RecDesc.builder()
        .add(Str.of("id"), Int64Desc.BASIC)
        .add(Str.of("company"), StrDesc.BASIC)
        .add(Str.of("last_name"), StrDesc.BASIC)
        .add(Str.of("first_name"), StrDesc.BASIC)
        .add(Str.of("email_address"), StrDesc.BASIC)
        .add(Str.of("job_title"), StrDesc.BASIC)
        .add(Str.of("business_phone"), StrDesc.BASIC)
        .add(Str.of("home_phone"), StrDesc.BASIC)
        .add(Str.of("mobile_phone"), StrDesc.BASIC)
        .add(Str.of("fax_number"), StrDesc.BASIC)
        .add(Str.of("address"), StrDesc.BASIC)
        .add(Str.of("city"), StrDesc.BASIC)
        .add(Str.of("state_province"), StrDesc.BASIC)
        .add(Str.of("zip_postal_code"), StrDesc.BASIC)
        .add(Str.of("country_region"), StrDesc.BASIC)
        .add(Str.of("web_page"), StrDesc.BASIC)
        .add(Str.of("notes"), StrDesc.BASIC)
        .add(Str.of("attachments"), StrDesc.BASIC)
        .build();

    public static final RecDesc ORDER_DESC = RecDesc.builder()
        .add(Str.of("id"), Int64Desc.BASIC)
        .add(Str.of("employee_id"), Int64Desc.BASIC)
        .add(Str.of("customer_id"), Int64Desc.BASIC)
        .add(Str.of("order_date"), DateDesc.BASIC)
        .add(Str.of("shipped_date"), DateDesc.BASIC)
        .add(Str.of("shipper_id"), Int64Desc.BASIC)
        .add(Str.of("ship_name"), StrDesc.BASIC)
        .add(Str.of("ship_address"), StrDesc.BASIC)
        .add(Str.of("ship_city"), StrDesc.BASIC)
        .add(Str.of("ship_state_province"), StrDesc.BASIC)
        .add(Str.of("ship_zip_postal_code"), Int64Desc.BASIC)
        .add(Str.of("ship_country_region"), StrDesc.BASIC)
        .add(Str.of("shipping_fee"), Dec128Desc.BASIC)
        .add(Str.of("taxes"), Dec128Desc.BASIC)
        .add(Str.of("payment_type"), StrDesc.BASIC)
        .add(Str.of("paid_date"), DateDesc.BASIC)
        .add(Str.of("notes"), StrDesc.BASIC)
        .add(Str.of("tax_rate"), Flt64Desc.BASIC)
        .add(Str.of("tax_status_id"), Int32Desc.BASIC)
        .add(Str.of("status_id"), Int32Desc.BASIC)
        .build();

    public static final RecDesc ORDER_DETAILS_DESC = RecDesc.builder()
        .add(Str.of("order_id"), Int64Desc.BASIC)
        .add(Str.of("line_seq"), Int64Desc.BASIC)
        .add(Str.of("product_id"), Int64Desc.BASIC)
        .add(Str.of("quantity"), Dec128Desc.BASIC)
        .add(Str.of("unit_price"), Dec128Desc.BASIC)
        .add(Str.of("discount"), Flt64Desc.BASIC)
        .add(Str.of("status_id"), Int64Desc.BASIC)
        .add(Str.of("date_allocated"), DateDesc.BASIC)
        .add(Str.of("purchase_order_id"), Int64Desc.BASIC)
        .add(Str.of("inventory_id"), Int64Desc.BASIC)
        .build();

    public static final RecDesc PRODUCT_DESC = RecDesc.builder()
        .add(Str.of("id"), Int64Desc.BASIC)
        .add(Str.of("supplier_ids"), StrDesc.BASIC)
        .add(Str.of("product_code"), StrDesc.BASIC)
        .add(Str.of("product_name"), StrDesc.BASIC)
        .add(Str.of("description"), StrDesc.BASIC)
        .add(Str.of("standard_cost"), Dec128Desc.BASIC)
        .add(Str.of("list_price"), Dec128Desc.BASIC)
        .add(Str.of("reorder_level"), Int64Desc.BASIC)
        .add(Str.of("target_level"), Int64Desc.BASIC)
        .add(Str.of("quantity_per_unit"), StrDesc.BASIC)
        .add(Str.of("discontinued"), BoolDesc.BASIC)
        .add(Str.of("minimum_reorder_quantity"), Int64Desc.BASIC)
        .add(Str.of("category"), StrDesc.BASIC)
        .build();

    public static final RecDesc SUPPLIER_DESC = RecDesc.builder()
        .add(Str.of("id"), Int64Desc.BASIC)
        .add(Str.of("company"), StrDesc.BASIC)
        .add(Str.of("last_name"), StrDesc.BASIC)
        .add(Str.of("first_name"), StrDesc.BASIC)
        .add(Str.of("email_address"), StrDesc.BASIC)
        .add(Str.of("job_title"), StrDesc.BASIC)
        .add(Str.of("business_phone"), StrDesc.BASIC)
        .add(Str.of("home_phone"), StrDesc.BASIC)
        .add(Str.of("mobile_phone"), StrDesc.BASIC)
        .add(Str.of("fax_number"), StrDesc.BASIC)
        .add(Str.of("address"), StrDesc.BASIC)
        .add(Str.of("city"), StrDesc.BASIC)
        .add(Str.of("state_province"), StrDesc.BASIC)
        .add(Str.of("zip_postal_code"), StrDesc.BASIC)
        .add(Str.of("country_region"), StrDesc.BASIC)
        .add(Str.of("web_page"), StrDesc.BASIC)
        .add(Str.of("notes"), StrDesc.BASIC)
        .build();

    public static final Map<String, RecDesc> NORTHWIND_DESCS_BY_ENTITY;

    static {
        HashMap<String, RecDesc> map = new HashMap<>();
        map.put(NorthwindFiles.CUSTOMERS_COLL_NAME, CUSTOMER_DESC);
        map.put(NorthwindFiles.EMPLOYEES_COLL_NAME, EMPLOYEE_DESC);
        map.put(NorthwindFiles.ORDERS_COLL_NAME, ORDER_DESC);
        map.put(NorthwindFiles.ORDER_DETAILS_COLL_NAME, ORDER_DETAILS_DESC);
        map.put(NorthwindFiles.PRODUCTS_COLL_NAME, PRODUCT_DESC);
        map.put(NorthwindFiles.SHIPPERS_COLL_NAME, SUPPLIER_DESC);
        NORTHWIND_DESCS_BY_ENTITY = Map.copyOf(map);
    }
}
