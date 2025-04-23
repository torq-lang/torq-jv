/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.util.FileBroker;
import org.torqlang.util.ReadTextFromResource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class ExamplesTorqBroker implements FileBroker {

    private static final List<Entry> CONTENT = List.of(
        new Entry(new Name(NameType.DIRECTORY, "org"), List.of(
            new Entry(new Name(NameType.DIRECTORY, "torqlang"), List.of(
                new Entry(new Name(NameType.DIRECTORY, "examples"), List.of(
                    new Entry(new Name(NameType.DIRECTORY, "torqsrc"), List.of(
                        new Entry(new Name(NameType.DIRECTORY, "northwind"), List.of(
                            new Entry(new Name(NameType.FILE, "CustomersHandler"), null),
                            new Entry(new Name(NameType.FILE, "EmployeesHandler"), null),
                            new Entry(new Name(NameType.FILE, "OrdersHandler"), null),
                            new Entry(new Name(NameType.FILE, "ProductsHandler"), null),
                            new Entry(new Name(NameType.FILE, "SuppliersHandler"), null)
                        ))
                    ))
                ))
            ))
        ))
    );

    @Override
    public final List<Name> list() {
        return CONTENT.stream().map(e -> e.name).collect(Collectors.toList());
    }

    private Entry findEntry(Name name, List<Entry> content) {
        return content.stream().filter(e -> e.name.equals(name)).findFirst().orElse(null);
    }

    private Entry findEntry(List<Name> subPath) {
        List<Entry> content = CONTENT;
        Entry entry = null;
        for (Name n : subPath) {
            entry = findEntry(n, content);
            if (entry == null) {
                break;
            }
            content = entry.children;
        }
        return entry;
    }

    @Override
    public final List<Name> list(List<Name> path) {
        Entry entry = findEntry(path);
        if (entry == null) {
            return null;
        }
        return entry.children.stream().map(e -> e.name).collect(Collectors.toList());
    }

    @Override
    public final String source(List<Name> path) throws IOException {
        String absolutePath = "/" + path.stream().map(Name::value).collect(Collectors.joining("/"));
        return ReadTextFromResource.apply(getClass(), absolutePath);
    }

    private record Entry(Name name, List<Entry> children) {
    }

}
