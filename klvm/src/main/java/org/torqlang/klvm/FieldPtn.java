/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public final class FieldPtn implements Decl {

    public final FeatureOrIdentPtn feature;
    public final ValueOrIdentPtn value;
    public final SourceSpan sourceSpan;

    public FieldPtn(FeatureOrIdentPtn feature, ValueOrIdentPtn value, SourceSpan sourceSpan) {
        this.feature = feature;
        this.value = value;
        this.sourceSpan = sourceSpan;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitFieldPtn(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        ValueOrIdentPtn.captureLexicallyFree(feature, knownBound, lexicallyFree);
        ValueOrIdentPtn.captureLexicallyFree(value, knownBound, lexicallyFree);
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
