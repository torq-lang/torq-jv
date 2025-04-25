/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.junit.jupiter.api.Test;
import org.torqlang.util.FileBroker;
import org.torqlang.util.FileName;
import org.torqlang.util.FileType;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestExamplesSourceBroker {

    @Test
    public void test01() throws IOException {
        FileBroker broker = ExamplesSourceBroker.createResourcesBrokerForModules();

        List<FileName> content = broker.list();
        assertEquals(1, content.size());
        FileName org = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "org"), org);

        content = broker.list(List.of(org));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName torqlang = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "torqlang"), torqlang);

        content = broker.list(List.of(org, torqlang));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName examples = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "examples"), examples);

        content = broker.list(List.of(org, torqlang, examples));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName torqsrc = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "torqsrc"), torqsrc);

        content = broker.list(List.of(org, torqlang, examples, torqsrc));
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName northwind = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "northwind"), northwind);

        content = broker.list(List.of(org, torqlang, examples, torqsrc, northwind));
        assertNotNull(content);
        assertEquals(6, content.size());
        assertTrue(content.contains(new FileName(FileType.TORQ, "CustomersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "EmployeesApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "NorthwindDb.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "OrdersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "ProductsApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "SuppliersApiHandler.torq")));

        String customersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileName(FileType.TORQ, "CustomersHandler.torq")));
        assertNotNull(customersHandler);
        assertTrue(customersHandler.contains("CustomersHandler"));

        String employeesHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileName(FileType.TORQ, "EmployeesHandler.torq")));
        assertNotNull(employeesHandler);
        assertTrue(employeesHandler.contains("EmployeesHandler"));

        String ordersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileName(FileType.TORQ, "OrdersHandler.torq")));
        assertNotNull(ordersHandler);
        assertTrue(ordersHandler.contains("OrdersHandler"));

        String productsHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileName(FileType.TORQ, "ProductsHandler.torq")));
        assertNotNull(productsHandler);
        assertTrue(productsHandler.contains("ProductsHandler"));

        String suppliersHandler = broker.source(List.of(org, torqlang, examples, torqsrc, northwind, new FileName(FileType.TORQ, "SuppliersHandler.torq")));
        assertNotNull(suppliersHandler);
        assertTrue(suppliersHandler.contains("SuppliersHandler"));
    }

}
