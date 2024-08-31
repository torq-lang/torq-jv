/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

/**
 * Kernel memory can only contain values. Therefore, all declarations must be converted to values. The process of
 * converting a declaration to a value occurs when a computation step must bind an identifier, procedure definition or
 * record definition to memory for subsequent computation. Converting a declaration to a value causes declarations and
 * identifiers to be associated with closures, values and memory variables (memory locations).
 */
public interface Decl extends DeclOrStmt {
}
