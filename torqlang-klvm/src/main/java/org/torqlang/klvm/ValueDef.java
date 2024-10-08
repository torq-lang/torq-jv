/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class ValueDef implements Decl {

    public final CompleteOrIdent value;
    public final SourceSpan sourceSpan;

    public ValueDef(CompleteOrIdent value, SourceSpan sourceSpan) {
        this.value = value;
        this.sourceSpan = sourceSpan;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitValueDef(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(value, knownBound, lexicallyFree);
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
