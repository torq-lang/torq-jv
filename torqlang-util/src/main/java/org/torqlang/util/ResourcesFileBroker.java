/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/*
 * A resources file broker has just one root.
 */
public final class ResourcesFileBroker implements FileBroker {

    private final Class<?> reference;
    private final List<List<FileName>> roots;
    private final List<Entry> content;

    public ResourcesFileBroker(Class<?> reference, List<FileName> root, List<Entry> content) {
        this.reference = reference;
        this.roots = List.of(ListTools.nullSafeCopyOf(root));
        this.content = content;
    }

    public final List<Entry> content() {
        return content;
    }

    @Override
    public final List<FileName> list() {
        return content.stream().map(e -> e.name).collect(Collectors.toList());
    }

    private Entry findEntry(FileName name, List<Entry> content) {
        return content.stream().filter(e -> e.name.equals(name)).findFirst().orElse(null);
    }

    private Entry findEntry(List<FileName> subPath) {
        List<Entry> content = content();
        Entry entry = null;
        for (FileName n : subPath) {
            entry = findEntry(n, content);
            if (entry == null) {
                break;
            }
            content = entry.children;
        }
        return entry;
    }

    @Override
    public final List<FileName> list(List<FileName> absolutePath) {
        Entry entry = findEntry(absolutePath);
        if (entry == null) {
            return null;
        }
        return entry.children.stream().map(e -> e.name).collect(Collectors.toList());
    }

    @Override
    public final List<List<FileName>> roots() {
        return roots;
    }

    @Override
    public final String source(List<FileName> path) throws IOException {
        String absolutePath = "/" + path.stream().map(FileName::value).collect(Collectors.joining("/"));
        return ReadTextFromResource.apply(reference, absolutePath);
    }

    public record Entry(FileName name, List<Entry> children) {
    }
}
