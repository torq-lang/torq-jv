/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.junit.jupiter.api.Test;
import org.torqlang.util.FileBroker;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestExamplesTorqBroker {

    @Test
    public void test01() throws IOException {
        ExamplesTorqBroker broker = new ExamplesTorqBroker();

        List<FileBroker.Name> content = broker.list();
        assertEquals(1, content.size());
        FileBroker.Name org = content.get(0);
        assertEquals(new FileBroker.Name(FileBroker.NameType.DIRECTORY, "org"), org);

        content = broker.list(List.of(org));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileBroker.Name torqlang = content.get(0);
        assertEquals(new FileBroker.Name(FileBroker.NameType.DIRECTORY, "torqlang"), torqlang);

        content = broker.list(List.of(org, torqlang));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileBroker.Name examples = content.get(0);
        assertEquals(new FileBroker.Name(FileBroker.NameType.DIRECTORY, "examples"), examples);

        content = broker.list(List.of(org, torqlang, examples));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileBroker.Name torqsrc = content.get(0);
        assertEquals(new FileBroker.Name(FileBroker.NameType.DIRECTORY, "torqsrc"), torqsrc);

        content = broker.list(List.of(org, torqlang, examples, torqsrc));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileBroker.Name northwind = content.get(0);
        assertEquals(new FileBroker.Name(FileBroker.NameType.DIRECTORY, "northwind"), northwind);

        content = broker.list(List.of(org, torqlang, examples, torqsrc, northwind));
        assertNotNull(content);
        assertEquals(5, content.size());
        assertTrue(content.contains(new FileBroker.Name(FileBroker.NameType.FILE, "CustomersHandler")));
        assertTrue(content.contains(new FileBroker.Name(FileBroker.NameType.FILE, "EmployeesHandler")));
        assertTrue(content.contains(new FileBroker.Name(FileBroker.NameType.FILE, "OrdersHandler")));
        assertTrue(content.contains(new FileBroker.Name(FileBroker.NameType.FILE, "ProductsHandler")));
        assertTrue(content.contains(new FileBroker.Name(FileBroker.NameType.FILE, "SuppliersHandler")));

        String customersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileBroker.Name(FileBroker.NameType.FILE, "CustomersHandler.torq")));
        assertNotNull(customersHandler);
        assertTrue(customersHandler.contains("CustomersHandler"));

        String employeesHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileBroker.Name(FileBroker.NameType.FILE, "EmployeesHandler.torq")));
        assertNotNull(employeesHandler);
        assertTrue(employeesHandler.contains("EmployeesHandler"));

        String ordersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileBroker.Name(FileBroker.NameType.FILE, "OrdersHandler.torq")));
        assertNotNull(ordersHandler);
        assertTrue(ordersHandler.contains("OrdersHandler"));

        String productsHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileBroker.Name(FileBroker.NameType.FILE, "ProductsHandler.torq")));
        assertNotNull(productsHandler);
        assertTrue(productsHandler.contains("ProductsHandler"));

        String suppliersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileBroker.Name(FileBroker.NameType.FILE, "SuppliersHandler.torq")));
        assertNotNull(suppliersHandler);
        assertTrue(suppliersHandler.contains("SuppliersHandler"));
    }

}
