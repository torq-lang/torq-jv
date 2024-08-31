/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public interface Kernel {

    static String toSystemString(Kernel kernel) {
        return kernel.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(kernel));
    }

    <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception;

    default String toKernelString() {
        return KernelFormatter.SINGLETON.format(this);
    }

}
