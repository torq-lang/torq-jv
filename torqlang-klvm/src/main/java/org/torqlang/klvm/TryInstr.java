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

public class TryInstr extends AbstractInstr {

    public final Instr body;
    public final Instr catchInstr;

    public TryInstr(Instr body, Instr catchInstr, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.body = body;
        this.catchInstr = catchInstr;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitTryInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        // Copy knownBound to hide out-of-scope identifiers from the catch instruction
        body.captureLexicallyFree(new HashSet<>(knownBound), lexicallyFree);
        // No need to copy knownBound since there are no more peer instructions
        catchInstr.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        // CRITICAL: CatchInstr cannot be nested inside another instruction, such as a 'local'
        catchInstr.pushStackEntries(machine, env);
        body.pushStackEntries(machine, env);
    }

}
