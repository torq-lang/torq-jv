/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface QuantType extends PolyType {
    static QuantType create(List<VarType> quantifiers, MonoType monoType) {
        return new QuantTypeImpl(quantifiers, monoType);
    }

    MonoType monoType();

    List<VarType> quantifiers();
}

@SuppressWarnings("ClassCanBeRecord")
final class QuantTypeImpl implements QuantType {

    private final List<VarType> quantifiers;
    private final MonoType monoType;

    QuantTypeImpl(List<VarType> quantifiers, MonoType monoType) {
        this.quantifiers = List.copyOf(quantifiers);
        this.monoType = monoType;
    }

    @Override
    public final PolyType addQuantifiers(Set<VarType> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            List<VarType> newQuants = new ArrayList<>(quantifiers);
            newQuants.addAll(freeVars);
            return QuantType.create(List.copyOf(newQuants), monoType);
        }
    }

    @Override
    public final void captureFreeVars(Set<VarType> freeVars) {
        Set<VarType> fvs = monoType.freeVars();
        quantifiers.forEach(fvs::remove);
        freeVars.addAll(fvs);
    }

    @Override
    public final Set<VarType> freeVars() {
        Set<VarType> freeVars = new HashSet<>();
        captureFreeVars(freeVars);
        return freeVars;
    }

    @Override
    public MonoType monoType() {
        return monoType;
    }

    @Override
    public List<VarType> quantifiers() {
        return quantifiers;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (VarType q : quantifiers) {
            sb.append(FOR_ALL_QUANT);
            sb.append(q.toString());
            sb.append(".");
        }
        sb.append(monoType.toString());
        return sb.toString();
    }
}