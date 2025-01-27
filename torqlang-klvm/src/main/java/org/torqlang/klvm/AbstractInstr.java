/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public abstract class AbstractInstr implements Instr {

    public final SourceSpan sourceSpan;

    public AbstractInstr(SourceSpan sourceSpan) {
        this.sourceSpan = sourceSpan;
    }

    @Override
    public abstract void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree);

    @Override
    public void pushStackEntries(Machine machine, Env env) {
        machine.pushStackEntry(this, env);
    }

    @Override
    public final int sourceBegin() {
        return sourceSpan.sourceBegin();
    }

    @Override
    public final int sourceEnd() {
        return sourceSpan.sourceEnd();
    }

    @Override
    public final String source() {
        return sourceSpan.source();
    }

    @Override
    public final SourceSpan toSourceBegin() {
        return sourceSpan.toSourceBegin();
    }

    @Override
    public final SourceSpan toSourceEnd() {
        return sourceSpan.toSourceEnd();
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
