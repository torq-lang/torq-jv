/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class SeqInstr extends AbstractInstr {

    public final InstrList seq;

    public SeqInstr(Iterable<Instr> instrs, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.seq = new InstrList(instrs);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitSeqInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        Instr.captureLexicallyFree(seq, knownBound, lexicallyFree);
    }

    @Override
    public void compute(Env env, Machine machine) {
        machine.pushStackEntries(seq, env);
    }

    @Override
    public void pushStackEntries(Machine machine, Env env) {
        machine.pushStackEntries(this.seq, env);
    }

}
