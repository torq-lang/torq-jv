/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.*;

public interface FuncInfr extends AppInfr {
    static FuncInfr create(List<MonoInfr> params) {
        return new FuncInfrImpl(params);
    }

    @Override
    FuncInfr subst(TypeSubst subst);
}

@SuppressWarnings("ClassCanBeRecord")
final class FuncInfrImpl implements FuncInfr {

    private final List<MonoInfr> params;

    FuncInfrImpl(List<MonoInfr> params) {
        this.params = params;
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
        for (MonoInfr param : params) {
            param.captureFreeVars(freeVars);
        }
    }

    @Override
    public final boolean contains(MonoInfr other) {
        if (this.equals(other)) {
            return true;
        }
        for (MonoInfr param : params) {
            if (param.contains(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        FuncInfrImpl that = (FuncInfrImpl) other;
        return Objects.equals(params, that.params);
    }

    @Override
    public final Set<VarInfr> freeVars() {
        Set<VarInfr> freeVars = new HashSet<>();
        captureFreeVars(freeVars);
        return freeVars;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(params);
    }

    @Override
    public final FuncInfr instantiate(SuffixFactory suffixFactory) {
        return this;
    }

    @Override
    public final String name() {
        return "Func";
    }

    @Override
    public final List<MonoInfr> params() {
        return params;
    }

    @Override
    public final FuncInfr subst(TypeSubst subst) {
        List<MonoInfr> mappedParams = new ArrayList<>();
        for (MonoInfr param : params) {
            mappedParams.add(param.subst(subst));
        }
        if (mappedParams.equals(params)) {
            return this;
        } else {
            return new FuncInfrImpl(mappedParams);
        }
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (int i = 0; i < params.size() - 1; i++) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(params.get(i).toString());
            first = false;
        }
        sb.append(")");
        sb.append(" -> ");
        sb.append(params.get(params.size() - 1).toString());
        return sb.toString();
    }

}