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

public interface ScalarInfr extends AppInfr {

    ScalarInfr BOOL = new ScalarInfrImpl("Bool");
    ScalarInfr INT32 = new ScalarInfrImpl("Int32");
    ScalarInfr INT64 = new ScalarInfrImpl("Int64");
    ScalarInfr STR = new ScalarInfrImpl("Str");
    ScalarInfr VOID = new ScalarInfrImpl("Void");

    static ScalarInfr create(String name) {
        return new ScalarInfrImpl(name);
    }

    @Override
    ScalarInfr subst(TypeSubst subst);
}

@SuppressWarnings("ClassCanBeRecord")
final class ScalarInfrImpl implements ScalarInfr {

    private final String name;

    ScalarInfrImpl(String name) {
        this.name = name;
    }

    @Override
    public final PolyInfr addQuantifiers(Set<VarInfr> freeVars) {
        if (freeVars.isEmpty()) {
            return this;
        } else {
            throw new IllegalStateException("Cannot quantify a scalar type");
        }
    }

    @Override
    public final void captureFreeVars(Set<VarInfr> freeVars) {
        // Scalar types have no free type variables
    }

    @Override
    public final boolean contains(MonoInfr other) {
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
        ScalarInfrImpl that = (ScalarInfrImpl) other;
        return Objects.equals(name, that.name);
    }

    @Override
    public final Set<VarInfr> freeVars() {
        return Collections.emptySet();
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public final ScalarInfr instantiate(SuffixFactory suffixFactory) {
        return this;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final List<MonoInfr> params() {
        return Collections.emptyList();
    }

    @Override
    public final ScalarInfr subst(TypeSubst subst) {
        return this;
    }

    @Override
    public final String toString() {
        return name;
    }

}