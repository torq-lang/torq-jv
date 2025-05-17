/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.Objects;

/*
 * A source string is a wrapper for a cached string. When accessed, a source string may need to restore its content
 * through a source broker.
 */
public interface SourceString {
    SourceString EMPTY_SOURCE_STRING = SourceString.of("");

    static SourceString of(String source) {
        return new SourceStringImpl(source);
    }

    char charAt(int index);

    boolean containsIndex(int index);

    String content();

    boolean isEmpty();

    String substring(int beginIndex, int endIndex);
}

@SuppressWarnings("ClassCanBeRecord")
final class SourceStringImpl implements SourceString {

    private final String content;

    SourceStringImpl(String content) {
        this.content = content;
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
        SourceStringImpl that = (SourceStringImpl) other;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public final boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public final String substring(int beginIndex, int endIndex) {
        return content.substring(beginIndex, endIndex);
    }
}