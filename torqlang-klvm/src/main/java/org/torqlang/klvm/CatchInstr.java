/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

/**
 * A CatchInstr is only used internally when a TryInstr pushes it onto the machine stack. A CatchInstr is
 * simply a holder for a CaseInstr.
 */
public class CatchInstr extends AbstractInstr {

    public final Ident arg;
    public final Instr caseInstr;

    public CatchInstr(Ident arg, Instr caseInstr, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.arg = arg;
        this.caseInstr = caseInstr;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitCatchInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        knownBound.add(arg);
        caseInstr.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        // Do nothing -- a catch instruction encountered in the normal course of processing is ignored
    }

}
