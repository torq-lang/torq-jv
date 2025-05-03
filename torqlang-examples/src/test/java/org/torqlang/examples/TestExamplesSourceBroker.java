/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.junit.jupiter.api.Test;
import org.torqlang.util.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.torqlang.examples.ExamplesSourceBroker.EXAMPLES_ROOT;

public class TestExamplesSourceBroker {

    @Test
    public void test01() throws IOException {
        SourceFileBroker broker = ExamplesSourceBroker.createResourcesBrokerForModules();

        assertEquals(1, broker.roots().size());

        List<FileName> content = broker.list(EXAMPLES_ROOT);
        assertNotNull(content);
        assertEquals(1, content.size());
        FileName northwind = content.get(0);
        assertEquals(new FileName(FileType.FOLDER, "northwind"), northwind);

        content = broker.list(SourceFileBroker.append(EXAMPLES_ROOT, northwind));
        assertNotNull(content);
        assertEquals(6, content.size());
        assertTrue(content.contains(new FileName(FileType.SOURCE, "CustomersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.SOURCE, "EmployeesApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.SOURCE, "NorthwindDb.torq")));
        assertTrue(content.contains(new FileName(FileType.SOURCE, "OrdersApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.SOURCE, "ProductsApiHandler.torq")));
        assertTrue(content.contains(new FileName(FileType.SOURCE, "SuppliersApiHandler.torq")));

        List<FileName> northwindRoot = SourceFileBroker.append(EXAMPLES_ROOT, northwind);

        SourceString customersHandler = broker.source(SourceFileBroker.append(northwindRoot, new FileName(FileType.SOURCE, "CustomersHandler.torq")));
        assertNotNull(customersHandler);
        assertTrue(customersHandler.content().contains("CustomersHandler"));

        SourceString employeesHandler = broker.source(SourceFileBroker.append(northwindRoot, new FileName(FileType.SOURCE, "EmployeesHandler.torq")));
        assertNotNull(employeesHandler);
        assertTrue(employeesHandler.content().contains("EmployeesHandler"));

        SourceString ordersHandler = broker.source(SourceFileBroker.append(northwindRoot, new FileName(FileType.SOURCE, "OrdersHandler.torq")));
        assertNotNull(ordersHandler);
        assertTrue(ordersHandler.content().contains("OrdersHandler"));

        SourceString productsHandler = broker.source(SourceFileBroker.append(northwindRoot, new FileName(FileType.SOURCE, "ProductsHandler.torq")));
        assertNotNull(productsHandler);
        assertTrue(productsHandler.content().contains("ProductsHandler"));

        SourceString suppliersHandler = broker.source(SourceFileBroker.append(northwindRoot, new FileName(FileType.SOURCE, "SuppliersHandler.torq")));
        assertNotNull(suppliersHandler);
        assertTrue(suppliersHandler.content().contains("SuppliersHandler"));
    }

}
