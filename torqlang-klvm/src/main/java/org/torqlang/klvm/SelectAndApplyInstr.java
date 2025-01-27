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

public final class SelectAndApplyInstr extends AbstractInstr {

    public final CompleteOrIdent rec;
    public final List<FeatureOrIdent> path;
    public final List<CompleteOrIdent> args;

    public SelectAndApplyInstr(CompleteOrIdent rec, List<FeatureOrIdent> path, List<CompleteOrIdent> args, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.rec = rec;
        this.path = path;
        this.args = nullSafeCopyOf(args);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitSelectAndApplyInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(rec, knownBound, lexicallyFree);
        for (FeatureOrIdent f : path) {
            CompleteOrIdent.captureLexicallyFree(f, knownBound, lexicallyFree);
        }
        for (CompleteOrIdent a : args) {
            CompleteOrIdent.captureLexicallyFree(a, knownBound, lexicallyFree);
        }
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        Value selectedValue = rec.resolveValue(env);
        for (FeatureOrIdent f : path) {
            Composite composite = (Composite) selectedValue;
            Feature featureRes = (Feature) f.resolveValue(env);
            selectedValue = composite.select(featureRes).resolveValue();
        }
        Proc p = (Proc) selectedValue;
        p.apply(args, env, machine);
    }

}
