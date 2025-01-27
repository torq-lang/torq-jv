/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

public interface DeclOrInstr extends Kernel {

    /*
     * Capture the lexically free identifiers from a kernel element, recursively. Each implementor must ensure that
     * all lexically free identifiers with respect to the given knownBound identifiers are added to the lexicallyFree
     * set. The knownBound set is empty at the start of a closure and its contents are used by nested instructions to
     * determine if its identifiers are already bound within the closure. Since the knownBound set is only used by
     * nested instructions, implementors are not required to add their bound identifiers if they already know their
     * lexically free identifiers. For example, nested procedure definitions already know their free identifiers.
     *
     * knownBound       the identifiers known so far to be bound in the current lexical scope
     * lexicallyFree    the free identifiers captured so far in the current lexical scope
     */
    void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree);

}
