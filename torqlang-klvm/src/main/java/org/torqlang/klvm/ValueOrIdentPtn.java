/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

/**
 *
 */
public interface ValueOrIdentPtn extends ValueOrPtn {

    static void captureLexicallyFree(ValueOrIdentPtn valueOrIdentPtn, Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        if (valueOrIdentPtn instanceof IdentPtn identPtn) {
            identPtn.captureLexicallyFree(knownBound, lexicallyFree);
        }
    }

    Value resolveValue(Env env) throws WaitException;

    ValueOrIdent resolveValueOrIdent(Env env) throws WaitException;

}
