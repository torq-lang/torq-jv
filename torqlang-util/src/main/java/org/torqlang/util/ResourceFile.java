/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.List;
import java.util.Objects;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

@SuppressWarnings("ClassCanBeRecord")
public final class ResourceFile implements SourceFile {

    private final ResourceFileBroker broker;
    private final List<FileName> path;
    private final String content;

    public ResourceFile(ResourceFileBroker broker, List<FileName> path, String content) {
        this.broker = broker;
        this.path = nullSafeCopyOf(path);
        this.content = content;
    }

    @Override
    public final ResourceFileBroker broker() {
        return broker;
    }

    @Override
    public final char charAt(int index) {
        return content.charAt(index);
    }

    @Override
    public final boolean containsIndex(int index) {
        return index > -1 && index < content.length();
    }

    @Override
    public final String content() {
        return content;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ResourceFile that = (ResourceFile) other;
        return Objects.equals(broker(), that.broker())
            && Objects.equals(path(), that.path())
            && Objects.equals(content(), that.content());
    }

    @Override
    public int hashCode() {
        return Objects.hash(broker, path, content());
    }

    @Override
    public final boolean isEmpty() {
        return content.isEmpty();
    }

    public final List<FileName> path() {
        return path;
    }

    @Override
    public final String substring(int beginIndex, int endIndex) {
        return content.substring(beginIndex, endIndex);
    }
}
