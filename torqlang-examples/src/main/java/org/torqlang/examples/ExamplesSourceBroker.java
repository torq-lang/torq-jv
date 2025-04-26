/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.ResourcesFileBroker;
import org.torqlang.util.ResourcesFileBroker.Entry;

import java.lang.invoke.MethodHandles;
import java.util.List;

public final class ExamplesSourceBroker {

    public static final List<Entry> CONTENT_ACTORS = List.of(
        new Entry(new FileName(FileType.DIRECTORY, "org"), List.of(
            new Entry(new FileName(FileType.DIRECTORY, "torqlang"), List.of(
                new Entry(new FileName(FileType.DIRECTORY, "examples"), List.of(
                    new Entry(new FileName(FileType.DIRECTORY, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.DIRECTORY, "northwind"), List.of(
                            new Entry(new FileName(FileType.TORQ, "CustomersHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "EmployeesHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "OrdersHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "ProductsHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "SuppliersHandler.torq"), null)
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final List<Entry> CONTENT_MODULES = List.of(
        new Entry(new FileName(FileType.DIRECTORY, "org"), List.of(
            new Entry(new FileName(FileType.DIRECTORY, "torqlang"), List.of(
                new Entry(new FileName(FileType.DIRECTORY, "examples"), List.of(
                    new Entry(new FileName(FileType.DIRECTORY, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.DIRECTORY, "northwind"), List.of(
                            new Entry(new FileName(FileType.TORQ, "CustomersApiHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "EmployeesApiHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "NorthwindDb.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "OrdersApiHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "ProductsApiHandler.torq"), null),
                            new Entry(new FileName(FileType.TORQ, "SuppliersApiHandler.torq"), null)
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final FileName ORG = new FileName(FileType.DIRECTORY, "org");
    public static final FileName TORQLANG = new FileName(FileType.DIRECTORY, "torqlang");
    public static final FileName EXAMPLES = new FileName(FileType.DIRECTORY, "examples");
    public static final FileName TORQSRC = new FileName(FileType.DIRECTORY, "torqsrc");
    public static final FileName NORTHWIND = new FileName(FileType.DIRECTORY, "northwind");

    public static final List<FileName> EXAMPLES_ROOT = List.of(ORG, TORQLANG, EXAMPLES, TORQSRC);

    public static ResourcesFileBroker createResourcesBrokerForModules() {
        return new ResourcesFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT_MODULES);
    }

    public static ResourcesFileBroker createResourcesBrokerForActors() {
        return new ResourcesFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT_ACTORS);
    }

}
