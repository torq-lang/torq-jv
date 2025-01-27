/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.List;
import java.util.Set;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class ApplyInstr extends AbstractInstr {

    public final CompleteOrIdent x;
    public final List<CompleteOrIdent> ys;

    public ApplyInstr(CompleteOrIdent x, List<CompleteOrIdent> ys, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.x = x;
        this.ys = nullSafeCopyOf(ys);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitApplyInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(x, knownBound, lexicallyFree);
        for (CompleteOrIdent y : ys) {
            CompleteOrIdent.captureLexicallyFree(y, knownBound, lexicallyFree);
        }
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        Proc p = (Proc) x.resolveValue(env);
        p.apply(ys, env, machine);
    }

}
