/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class CreateRecInstr extends AbstractInstr implements CreateInstr {

    public final Ident x;
    public final RecDef recDef;

    public CreateRecInstr(Ident x, RecDef recDef, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.x = x;
        this.recDef = recDef;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitCreateRecInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        Ident.captureLexicallyFree(x, knownBound, lexicallyFree);
        recDef.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        PartialRecBuilder builder = Rec.partialRecBuilder();
        LiteralOrVar labelRes = (LiteralOrVar) recDef.label.resolveValueOrVar(env);
        builder.setLabel(labelRes);
        for (FieldDef fd : recDef.fieldDefs) {
            FeatureOrVar f = (FeatureOrVar) fd.feature.resolveValueOrVar(env);
            ValueOrVar v = fd.value.resolveValueOrVar(env);
            builder.addField(f, v);
        }
        Rec rec = builder.build();
        ValueOrVar xRes = x.resolveValueOrVar(env);
        xRes.bindToValue(rec, null);
    }

}
