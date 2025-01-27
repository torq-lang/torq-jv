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

public final class LocalInstr extends AbstractInstr {

    public final List<IdentDef> xs;
    public final Instr body;

    public LocalInstr(List<IdentDef> xs, Instr body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.xs = nullSafeCopyOf(xs);
        this.body = body;
        validate();
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitLocalInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        for (IdentDef idef : xs) {
            idef.captureLexicallyFree(knownBound, lexicallyFree);
        }
        body.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public void compute(Env env, Machine machine) {
        EnvEntry[] localBindings = new EnvEntry[xs.size()];
        int index = 0;
        for (IdentDef id : xs) {
            Var var = id.value != null ? new Var(id.value) : new Var();
            localBindings[index++] = new EnvEntry(id.ident, var);
        }
        Env bodyEnv = Env.createPrivatelyForKlvm(env, localBindings);
        body.pushStackEntries(machine, bodyEnv);
    }

    private void validate() {
        for (int i = 0; i < xs.size(); i++) {
            for (int j = i + 1; j < xs.size(); j++) {
                if (xs.get(i).equals(xs.get(j))) {
                    throw new DuplicateIdentError(xs.get(i).ident);
                }
            }
        }
    }

}
