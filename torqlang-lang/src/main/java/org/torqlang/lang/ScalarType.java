/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface ScalarType extends AppType {

    ScalarType BOOL = new ScalarTypeImpl("Bool");
    ScalarType INT32 = new ScalarTypeImpl("Int32");
    ScalarType INT64 = new ScalarTypeImpl("Int64");
    ScalarType STR = new ScalarTypeImpl("Str");
    ScalarType VOID = new ScalarTypeImpl("Void");

    static ScalarType create(String name) {
        return new ScalarTypeImpl(name);
    }

    @Override
    ScalarType subst(TypeSubst subst);
}

@SuppressWarnings("ClassCanBeRecord")
final class ScalarTypeImpl implements ScalarType {

    private final String name;

    ScalarTypeImpl(String name) {
        this.name = name;
    }

    @Override
    public final PolyType addQuantifiers(Set<VarType> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            throw new IllegalStateException("Cannot quantify a scalar type");
        }
    }

    @Override
    public final void captureFreeVars(Set<VarType> freeVars) {
        // Scalar types have no free type variables
    }

    @Override
    public final boolean contains(MonoType other) {
        return this == other;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ScalarTypeImpl that = (ScalarTypeImpl) other;
        return Objects.equals(name, that.name);
    }

    @Override
    public final Set<VarType> freeVars() {
        return Collections.emptySet();
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public final ScalarType instantiate(SuffixFactory suffixFactory) {
        return this;
    }

    public final String name() {
        return name;
    }

    @Override
    public final List<MonoType> params() {
        return Collections.emptyList();
    }

    @Override
    public final ScalarType subst(TypeSubst subst) {
        return this;
    }

    @Override
    public final String toString() {
        return name;
    }

}