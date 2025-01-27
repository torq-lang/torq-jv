/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class BindIdentToIdentInstr extends AbstractInstr implements BindInstr {

    public final Ident a;
    public final Ident x;

    public BindIdentToIdentInstr(Ident a, Ident x, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.a = a;
        this.x = x;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitBindIdentToIdentInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        Ident.captureLexicallyFree(a, knownBound, lexicallyFree);
        Ident.captureLexicallyFree(x, knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {

        // Variable-variable binding [CTM p. 63]
        // Value creation [CTM p. 63]

        // CRITICAL: Within this method, DO NOT resolve identifiers to their Value -- stop at Var. We must unify
        //           on Vars so that matching values become just one value in memory.

        Var aVar = env.get(a);
        if (aVar == null) {
            throw new IdentNotFoundError(a, this);
        }
        Var bVar = env.get(x);
        if (bVar == null) {
            throw new IdentNotFoundError(x, this);
        }
        aVar.bindToVar(bVar, null);
    }

}
