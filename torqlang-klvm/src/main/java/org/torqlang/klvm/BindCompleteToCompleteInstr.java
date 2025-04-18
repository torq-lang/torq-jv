/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class BindCompleteToCompleteInstr extends AbstractInstr implements BindInstr {

    public final Complete a;
    public final Complete x;

    public BindCompleteToCompleteInstr(Complete a, Complete x, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.a = a;
        this.x = x;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitBindCompleteToCompleteInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        a.bindToValue(x, null);
    }

}
