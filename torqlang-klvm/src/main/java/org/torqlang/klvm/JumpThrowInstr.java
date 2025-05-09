/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public class JumpThrowInstr extends AbstractInstr {

    public final int id;

    public JumpThrowInstr(int id, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.id = id;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitJumpThrowInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        // There are no identifiers and there is nothing to do
    }

    @Override
    public final void compute(Env env, Machine machine) {
        machine.unwindToJumpCatchInstr(this);
    }

}
