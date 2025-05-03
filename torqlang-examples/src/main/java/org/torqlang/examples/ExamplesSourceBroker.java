/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.ResourceFileBroker;
import org.torqlang.util.ResourceFileBroker.Entry;

import java.lang.invoke.MethodHandles;
import java.util.List;

public final class ExamplesSourceBroker {

    public static final List<Entry> CONTENT_ACTORS = List.of(
        new Entry(new FileName(FileType.FOLDER, "org"), List.of(
            new Entry(new FileName(FileType.FOLDER, "torqlang"), List.of(
                new Entry(new FileName(FileType.FOLDER, "examples"), List.of(
                    new Entry(new FileName(FileType.FOLDER, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.FOLDER, "northwind"), List.of(
                            new Entry(new FileName(FileType.SOURCE, "CustomersHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "EmployeesHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "OrdersHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "ProductsHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "SuppliersHandler.torq"), null)
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final List<Entry> CONTENT_MODULES = List.of(
        new Entry(new FileName(FileType.FOLDER, "org"), List.of(
            new Entry(new FileName(FileType.FOLDER, "torqlang"), List.of(
                new Entry(new FileName(FileType.FOLDER, "examples"), List.of(
                    new Entry(new FileName(FileType.FOLDER, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.FOLDER, "northwind"), List.of(
                            new Entry(new FileName(FileType.SOURCE, "CustomersApiHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "EmployeesApiHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "NorthwindDb.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "OrdersApiHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "ProductsApiHandler.torq"), null),
                            new Entry(new FileName(FileType.SOURCE, "SuppliersApiHandler.torq"), null)
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final FileName ORG = new FileName(FileType.FOLDER, "org");
    public static final FileName TORQLANG = new FileName(FileType.FOLDER, "torqlang");
    public static final FileName EXAMPLES = new FileName(FileType.FOLDER, "examples");
    public static final FileName TORQSRC = new FileName(FileType.FOLDER, "torqsrc");
    public static final FileName NORTHWIND = new FileName(FileType.FOLDER, "northwind");

    public static final List<FileName> EXAMPLES_ROOT = List.of(ORG, TORQLANG, EXAMPLES, TORQSRC);

    public static ResourceFileBroker createResourcesBrokerForModules() {
        return new ResourceFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT_MODULES);
    }

    public static ResourceFileBroker createResourcesBrokerForActors() {
        return new ResourceFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT_ACTORS);
    }

}
