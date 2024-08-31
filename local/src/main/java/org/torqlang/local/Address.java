/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

public interface Address extends Comparable<Address> {

    Address UNDEFINED = create("undefined");

    static Address create(Address parent, String path) {
        return LocalAddress.create(parent, path);
    }

    static Address create(String path) {
        return LocalAddress.create(path);
    }

    String path();

}
