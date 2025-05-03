/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

public interface Result<V, E> {

    static <V, E> Result<V, E> error(E left) {
        return new Error<>(left);
    }

    static <V, E> Result<V, E> value(V right) {
        return new Value<>(right);
    }

    E error();

    V value();

    boolean isError();

    boolean isValue();

    @SuppressWarnings("ClassCanBeRecord")
    final class Error<V, E> implements Result<V, E> {
        private final E error;

        public Error(E error) {
            this.error = error;
        }

        public boolean isError() {
            return true;
        }

        public boolean isValue() {
            return false;
        }

        @Override
        public E error() {
            return error;
        }

        public V value() {
            return null;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    final class Value<V, E> implements Result<V, E> {
        private final V value;

        public Value(V value) {
            this.value = value;
        }

        public boolean isError() {
            return false;
        }

        public boolean isValue() {
            return true;
        }

        @Override
        public E error() {
            return null;
        }

        public V value() {
            return value;
        }
    }
}
