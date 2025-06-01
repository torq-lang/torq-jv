/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

/*
 * A common set of scalar conversions:
 *     toString()
 *         A meaningful string representation of the object's state. In general, we mimic the Java toString() where
 *         applicable.
 *     toKernelString()
 *         A parsable value that can appear in a kernel language program. Strings are single-quoted, 32-bit floats have
 *         an "f" suffix, 64-bit longs have an "L" suffix, and 128-bit decimals have an "m" suffix, etc.
 *     toNativeValue()
 *         A Java value that most closely represents the kernel value. In general, the kernel value is a wrapper for a
 *         Java value, so this method returns the contained value.
 *     formatAsKernelString()
 *         Effectively, this is the same as toKernelString(), but implemented by scalar values, literal objects,
 *         and places where we want to print a parsable value in a kernel language program.
 */
public interface FormatAsKernelString {
    String formatAsKernelString();
}
