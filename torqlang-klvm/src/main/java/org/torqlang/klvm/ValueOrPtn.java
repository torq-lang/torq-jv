/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

public interface ValueOrPtn extends Kernel {

    static void captureLexicallyFree(ValueOrPtn valueOrPtn, Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        if (valueOrPtn instanceof Ptn ptn) {
            ptn.captureLexicallyFree(knownBound, lexicallyFree);
        }
    }

    /*
     * This is a callback requesting "case Value of ValueOrPtn then..."
     */
    ValueOrResolvedPtn caseNonRecOfThis(Value nonRecValue, Env env) throws WaitException;

    /*
     * This is a callback requesting "case Rec of ValueOrPtn then..."
     */
    ValueOrResolvedPtn caseRecOfThis(Rec rec, Env env) throws WaitException;

}
