/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.*;

public interface QuantInfr extends PolyInfr {
    static QuantInfr create(List<VarInfr> quantifiers, MonoInfr monoType) {
        return new QuantInfrImpl(quantifiers, monoType);
    }

    MonoInfr monoType();

    List<VarInfr> quantifiers();

    QuantInfr subst(TypeSubst subst);
}

@SuppressWarnings("ClassCanBeRecord")
final class QuantInfrImpl implements QuantInfr {

    private final List<VarInfr> quantifiers;
    private final MonoInfr monoType;

    QuantInfrImpl(List<VarInfr> quantifiers, MonoInfr monoType) {
        this.quantifiers = List.copyOf(quantifiers);
        this.monoType = monoType;
    }

    @Override
    public final PolyInfr addQuantifiers(Set<VarInfr> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            List<VarInfr> newQuants = new ArrayList<>(quantifiers);
            newQuants.addAll(freeVars);
            return QuantInfr.create(List.copyOf(newQuants), monoType);
        }
    }

    @Override
    public final void captureFreeVars(Set<VarInfr> freeVars) {
        Set<VarInfr> fvs = monoType.freeVars();
        quantifiers.forEach(fvs::remove);
        freeVars.addAll(fvs);
    }

    @Override
    public final Set<VarInfr> freeVars() {
        Set<VarInfr> freeVars = new HashSet<>();
        captureFreeVars(freeVars);
        return freeVars;
    }

    @Override
    public final MonoInfr instantiate(SuffixFactory suffixFactory) {
        Map<VarInfr, MonoInfr> mappings = new HashMap<>();
        for (VarInfr quantType : quantifiers) {
            mappings.put(quantType, suffixFactory.nextBetaVar());
        }
        TypeSubst subst = TypeSubst.create(mappings);
        return monoType.subst(subst);
    }

    @Override
    public final MonoInfr monoType() {
        return monoType;
    }

    @Override
    public final String name() {
        return monoType.name();
    }

    @Override
    public final List<VarInfr> quantifiers() {
        return quantifiers;
    }

    @Override
    public final QuantInfr subst(TypeSubst subst) {
        return new QuantInfrImpl(quantifiers, monoType.subst(subst));
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (VarInfr q : quantifiers) {
            sb.append(FOR_ALL_QUANT);
            sb.append(q.toString());
            sb.append(".");
        }
        sb.append(' ');
        sb.append(monoType.toString());
        return sb.toString();
    }
}