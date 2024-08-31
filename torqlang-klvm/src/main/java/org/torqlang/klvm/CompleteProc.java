/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

/*
 * When CompleteProc is used as a functional interface, the function is inheriting:
 *     - entails from Value, which compares on identity using `==`
 *     - equals from Object, which compares on identity using `==`
 *     - hashCode from Object, which is an native method returning identity
 *     - isValidKey from CompleteProc, which returns true
 */
public interface CompleteProc extends Complete, Proc {
    @Override
    default boolean isValidKey() {
        return true;
    }
}
