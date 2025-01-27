/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class NegateInstr extends AbstractInstr {

    public final CompleteOrIdent a;
    public final Ident x;

    public NegateInstr(CompleteOrIdent a, Ident x, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.a = a;
        this.x = x;
    }

    public final CompleteOrIdent a() {
        return a;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitNegateInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(a, knownBound, lexicallyFree);
        Ident.captureLexicallyFree(x, knownBound, lexicallyFree);
    }

    @Override
    public void compute(Env env, Machine machine) throws WaitException {
        Value aRes = a.resolveValue(env);
        Value result = aRes.negate();
        ValueOrVar xRes = x.resolveValueOrVar(env);
        xRes.bindToValue(result, null);
    }

    public final Ident x() {
        return x;
    }
}
