/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

/*
 * A debug instruction behaves like a cursor. As a machine executes, the debug instruction gives the debug listener a
 * chance to act on each instruction before it computes.
 */
public final class DebugInstr extends AbstractInstr {

    private final DebugInstrListener listener;

    private Instr nextInstr;
    private Env nextEnv;

    public DebugInstr(DebugInstrListener listener, Instr nextInstr, Env nextEnv, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.nextInstr = nextInstr;
        this.nextEnv = nextEnv;
        this.listener = listener;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitDebugInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        nextInstr.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        if (listener != null) {
            listener.onNextInstr(this.nextInstr, this.nextEnv, machine);
        }
        nextInstr.compute(nextEnv, machine);
        Stack next = machine.popStackEntry();
        if (next != null) {
            nextInstr = next.instr;
            nextEnv = next.env;
            machine.pushStackEntry(this, Env.emptyEnv());
        }
    }

    public final Env nextEnv() {
        return nextEnv;
    }

    public final Instr nextInstr() {
        return nextInstr;
    }

}
