/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.HashSet;
import java.util.Set;

public interface Instr extends DeclOrInstr, SourceSpan {

    /*
     * This is a convenience method. Capture the lexically free identifiers from a collection of instructions.
     * Free identifiers are captured from each peer instruction by resetting the knownBound set to the original
     * set passed to this method.
     *
     * instrs         collection of instructions from which we are collecting free identifiers
     * knownBound     identifiers known so far to be bound in the closure
     * lexicallyFree  free identifiers captured so far in the closure
     */
    static void captureLexicallyFree(InstrList instrs, Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        for (InstrList.Entry current = instrs.firstEntry(); current != null; current = current.next()) {
            // Reset knownBound for each peer instruction
            current.instr().captureLexicallyFree(new HashSet<>(knownBound), lexicallyFree);
        }
    }

    void compute(Env env, Machine machine) throws WaitException;

    void pushStackEntries(Machine machine, Env env);

}
