/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

@SuppressWarnings("ClassCanBeRecord")
public final class ResolvedFieldPtn implements Kernel, FeatureProvider {

    public final Feature feature;
    public final ValueOrIdent value;

    public ResolvedFieldPtn(Feature feature, ValueOrIdent value) {
        this.feature = feature;
        this.value = value;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitResolvedFieldPtn(this, state);
    }

    @Override
    public final Feature feature() {
        return feature;
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
