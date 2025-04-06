/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * Type Substitutions
 *
 * Hindley-Milner type inference algorithms use substitutions from type variables to monotypes. The substitutions are
 * applied on types.
 *
 * Instances of this class are immutable.
 */
public final class TypeSubst {
    private static final TypeSubst EMPTY_SUBST = new TypeSubst();

    private final Map<VarInfr, MonoInfr> mappings;

    private TypeSubst() {
        this(Collections.emptyMap());
    }

    private TypeSubst(Map<VarInfr, MonoInfr> mappings) {
        this.mappings = mappings;
    }

    public static TypeSubst combine(TypeSubst S1, TypeSubst S2) {
        Map<VarInfr, MonoInfr> combinedMapping = new HashMap<>();
        for (VarInfr key : S1.mappings.keySet()) {
            combinedMapping.put(key, key);
        }
        for (VarInfr key : S2.mappings.keySet()) {
            combinedMapping.put(key, key);
        }
        for (VarInfr key : combinedMapping.keySet()) {
            MonoInfr s2Type = S2.get(key);
            if (s2Type == null) {
                combinedMapping.put(key, S1.get(key));
            } else {
                combinedMapping.put(key, s2Type.subst(S1));
            }
        }
        return new TypeSubst(combinedMapping);
    }

    public static TypeSubst create(Map<VarInfr, MonoInfr> mappings) {
        return new TypeSubst(Map.copyOf(mappings));
    }

    public static TypeSubst create(VarInfr a, MonoInfr b) {
        return new TypeSubst(Map.of(a, b));
    }

    public static TypeSubst empty() {
        return EMPTY_SUBST;
    }

    /*
     * S = unify(a, b)
     * S(a) = S(b)
     */
    public static TypeSubst unify(MonoInfr a, MonoInfr b) {
        if (a instanceof VarInfr aVar) {
            if (a.equals(b)) {
                return EMPTY_SUBST;
            } else if (b.contains(a)) {
                throw new IllegalArgumentException("Occurs check failed, cannot create an infinite type");
            } else {
                return TypeSubst.create(aVar, b);
            }
        }
        if (b instanceof VarInfr) {
            return unify(b, a);
        }
        AppInfr aAppType = (AppInfr) a;
        AppInfr bAppType = (AppInfr) b;
        if (aAppType.getClass() == bAppType.getClass() &&
            aAppType.name().equals(bAppType.name()) &&
            aAppType.params().size() == bAppType.params().size())
        {
            if (aAppType.params().isEmpty()) {
                return EMPTY_SUBST;
            } else {
                TypeSubst subst = EMPTY_SUBST;
                for (int i = 0; i < aAppType.params().size(); i++) {
                    MonoInfr aParam = aAppType.params().get(i);
                    MonoInfr bParam = bAppType.params().get(i);
                    subst = combine(subst, unify(aParam.subst(subst), bParam.subst(subst)));
                }
                return subst;
            }
        } else {
            throw new TypeUnificationError(a, b);
        }
    }

    public final MonoInfr apply(MonoInfr monoType) {
        return monoType.subst(this);
    }

    public final TypeEnv apply(TypeEnv typeEnv) {
        typeEnv.subst(this);
        return typeEnv;
    }

    public final TypeSubst apply(TypeSubst typeSubst) {
        return TypeSubst.combine(this, typeSubst);
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        TypeSubst typeSubst = (TypeSubst) other;
        return Objects.equals(mappings, typeSubst.mappings);
    }

    public final MonoInfr get(VarInfr varType) {
        return mappings.get(varType);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(mappings);
    }

    public final void put(VarInfr varType, MonoInfr monoType) {
        mappings.put(varType, monoType);
    }

    public final int size() {
        return mappings.size();
    }
}
