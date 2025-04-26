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
import static org.torqlang.examples.ExamplesSourceBroker.EXAMPLES_ROOT;

public class TestExamplesSourceBroker {

    @Test
    public void test01() throws IOException {
        FileBroker broker = ExamplesSourceBroker.createResourcesBrokerForModules();

        assertEquals(1, broker.roots().size());

        List<FileName> content = broker.list(EXAMPLES_ROOT);
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName northwind = content.get(0);
        assertEquals(new FileName(FileType.DIRECTORY, "northwind"), northwind);

        content = broker.list(FileBroker.append(EXAMPLES_ROOT, northwind));
        assertNotNull(content);
        assertEquals(6, content.size());
        assertTrue(content.contains(new FileName(FileType.TORQ, "CustomersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "EmployeesApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "NorthwindDb.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "OrdersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "ProductsApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.TORQ, "SuppliersApiHandler.torq")));

        List<FileName> northwindRoot = FileBroker.append(EXAMPLES_ROOT, northwind);

        String customersHandler = broker.source(FileBroker.append(northwindRoot, new FileName(FileType.TORQ, "CustomersHandler.torq")));
        assertNotNull(customersHandler);
        assertTrue(customersHandler.contains("CustomersHandler"));

        String employeesHandler = broker.source(FileBroker.append(northwindRoot, new FileName(FileType.TORQ, "EmployeesHandler.torq")));
        assertNotNull(employeesHandler);
        assertTrue(employeesHandler.contains("EmployeesHandler"));

        String ordersHandler = broker.source(FileBroker.append(northwindRoot, new FileName(FileType.TORQ, "OrdersHandler.torq")));
        assertNotNull(ordersHandler);
        assertTrue(ordersHandler.contains("OrdersHandler"));

        String productsHandler = broker.source(FileBroker.append(northwindRoot, new FileName(FileType.TORQ, "ProductsHandler.torq")));
        assertNotNull(productsHandler);
        assertTrue(productsHandler.contains("ProductsHandler"));

        String suppliersHandler = broker.source(FileBroker.append(northwindRoot, new FileName(FileType.TORQ, "SuppliersHandler.torq")));
        assertNotNull(suppliersHandler);
        assertTrue(suppliersHandler.contains("SuppliersHandler"));
    }

}
