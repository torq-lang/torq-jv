/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.*;

public interface FuncType extends AppType {
    static FuncType create(List<MonoType> params) {
        return new FuncTypeImpl(params);
    }

    @Override
    FuncType subst(TypeSubst subst);
}

@SuppressWarnings("ClassCanBeRecord")
final class FuncTypeImpl implements FuncType {

    private final List<MonoType> params;

    FuncTypeImpl(List<MonoType> params) {
        this.params = params;
    }

    @Override
    public final PolyType addQuantifiers(Set<VarType> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            return QuantType.create(List.copyOf(freeVars), this);
        }
    }

    @Override
    public final void captureFreeVars(Set<VarType> freeVars) {
        for (MonoType param : params) {
            param.captureFreeVars(freeVars);
        }
    }

    @Override
    public final boolean contains(MonoType other) {
        if (this.equals(other)) {
            return true;
        }
        for (MonoType param : params) {
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
        FuncTypeImpl that = (FuncTypeImpl) other;
        return Objects.equals(params, that.params);
    }

    @Override
    public final Set<VarType> freeVars() {
        Set<VarType> freeVars = new HashSet<>();
        captureFreeVars(freeVars);
        return freeVars;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(params);
    }

    @Override
    public final FuncType instantiate(SuffixFactory suffixFactory) {
        return this;
    }

    @Override
    public final String name() {
        return "Func";
    }

    @Override
    public final List<MonoType> params() {
        return params;
    }

    @Override
    public final FuncType subst(TypeSubst subst) {
        List<MonoType> mappedParams = new ArrayList<>();
        for (MonoType param : params) {
            mappedParams.add(param.subst(subst));
        }
        if (mappedParams.equals(params)) {
            return this;
        } else {
            return new FuncTypeImpl(mappedParams);
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