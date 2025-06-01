/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.ListTools;
import org.torqlang.util.SourceSpan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SeqInstr extends AbstractInstr {

    public final List<Instr> list;

    public SeqInstr(List<Instr> list, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.list = ListTools.nullSafeCopyOf(list);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitSeqInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        /*
         * Capture the lexically free identifiers from a collection of instructions. Free identifiers are captured from
         * each peer instruction by resetting the knownBound set to the original set passed to this method.
         *
         * instrs         collection of instructions from which we are collecting free identifiers
         * knownBound     identifiers known so far to be bound in the closure
         * lexicallyFree  free identifiers captured so far in the closure
         */
        for (Instr instr : list) {
            instr.captureLexicallyFree(new HashSet<>(knownBound), lexicallyFree);
        }
    }

    @Override
    public void compute(Env env, Machine machine) {
        machine.pushStackEntries(list, env);
    }

    @Override
    public void pushStackEntries(Machine machine, Env env) {
        machine.pushStackEntries(this.list, env);
    }

}
