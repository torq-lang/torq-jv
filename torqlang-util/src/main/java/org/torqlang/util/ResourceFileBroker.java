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

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class ResourceFileBroker implements SourceFileBroker {

    private final Class<?> reference;
    private final List<List<FileName>> roots;
    private final List<Entry> content;

    public ResourceFileBroker(Class<?> reference, List<List<FileName>> roots, List<Entry> content) {
        this.reference = reference;
        this.roots = SourceFileBroker.checkForDuplicates(nullSafeCopyOf(roots.stream().map(ListTools::nullSafeCopyOf).toList()));
        this.content = content;
    }

    public final List<Entry> content() {
        return content;
    }

    @Override
    public final List<FileName> list(List<FileName> absolutePath) {
        List<Entry> content = content();
        for (FileName fileName : absolutePath) {
            boolean found = false;
            for (Entry entry : content) {
                if (fileName.value().equals(entry.name.value())) {
                    found = true;
                    content = entry.children;
                    break;
                }
            }
            if (!found) {
                content = null;
                break;
            }
        }
        if (content != null) {
            return nullSafeCopyOf(content.stream().map(e -> e.name).toList());
        } else {
            return null;
        }
    }

    @Override
    public final List<List<FileName>> roots() {
        return roots;
    }

    @Override
    public final ResourceFile source(List<FileName> path) throws IOException {
        String absolutePath = "/" + path.stream().map(FileName::value).collect(Collectors.joining("/"));
        String content = ReadTextFromResource.apply(reference, absolutePath);
        return new ResourceFile(this, path, content);
    }

    public record Entry(FileName name, List<Entry> children) {
    }
}
