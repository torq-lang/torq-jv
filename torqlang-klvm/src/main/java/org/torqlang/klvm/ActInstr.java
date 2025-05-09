/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Collections;
import java.util.Set;

public class ActInstr extends AbstractInstr {

    public final Instr instr;
    public final Ident target;

    public ActInstr(Instr instr, Ident target, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.instr = instr;
        this.target = target;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitActInstr(this, state);
    }

    @Override
    public void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        Ident.captureLexicallyFree(Ident.$ACT, knownBound, lexicallyFree);
        instr.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public void compute(Env env, Machine machine) throws WaitException {
        Proc act = (Proc) env.get(Ident.$ACT).resolveValue();
        act.apply(Collections.emptyList(), env, machine);
    }

}
