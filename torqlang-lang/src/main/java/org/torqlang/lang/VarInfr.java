/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface VarInfr extends MonoInfr {
    static VarInfr create(String name) {
        return new VarInfrImpl(name);
    }

    /*
     * The name of the variable, such as 'α', 'β', or 'γ'.
     */
    @Override
    String name();
}

@SuppressWarnings("ClassCanBeRecord")
final class VarInfrImpl implements VarInfr {
    private final String name;

    public VarInfrImpl(String name) {
        this.name = name;
    }

    @Override
    public final PolyInfr addQuantifiers(Set<VarInfr> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            return QuantInfr.create(List.copyOf(freeVars), this);
        }
    }

    @Override
    public final void captureFreeVars(Set<VarInfr> freeVars) {
        freeVars.add(this);
    }

    @Override
    public final boolean contains(MonoInfr other) {
        return this.equals(other);
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        VarInfrImpl that = (VarInfrImpl) other;
        return Objects.equals(name, that.name);
    }

    @Override
    public final Set<VarInfr> freeVars() {
        Set<VarInfr> freeVars = new HashSet<>();
        freeVars.add(this);
        return freeVars;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public final MonoInfr instantiate(SuffixFactory suffixFactory) {
        return this;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final MonoInfr subst(TypeSubst subst) {
        MonoInfr s = subst.get(this);
        return s != null ? s : this;
    }

    @Override
    public final String toString() {
        return name;
    }
}