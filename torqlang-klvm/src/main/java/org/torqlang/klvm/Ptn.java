/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

public interface Ptn extends Decl, ValueOrPtn {

    @Override
    void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree);

}
