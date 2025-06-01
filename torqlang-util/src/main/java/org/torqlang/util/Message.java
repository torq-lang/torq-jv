/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.util.Objects;

/*
 * A general message structure compatible with OpenAPI best practices. If possible, users should use the types defined
 * in MessageType. However, the type field is not constrained to a particular domain of values so that messages can be
 * adapted to existing systems.
 */
public interface Message {
    static Message create(String name, String type, String message, String details, String traceId) {
        return new MessageImpl(name, type, message, details, traceId);
    }

    static Message create(String name, MessageType type, String message, String details, String traceId) {
        return new MessageImpl(name, type.name(), message, details, traceId);
    }

    static Message create(String name, MessageType type, String message) {
        return new MessageImpl(name, type.name(), message, null, null);
    }

    String details();

    String message();

    String name();

    String traceId();

    String type();
}

record MessageImpl(String name, String type, String message, String details, String traceId) implements Message {
    MessageImpl {
        Objects.requireNonNull(name);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MessageImpl that = (MessageImpl) other;
        return Objects.equals(name, that.name) &&
            Objects.equals(type, that.type) &&
            Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, message);
    }
}