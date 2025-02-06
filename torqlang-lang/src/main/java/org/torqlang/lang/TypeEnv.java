/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;

import java.util.*;

/*
 * A mapping from identifiers to PolyTypes
 */
public interface TypeEnv {

    Ident ADD_IDENT = Ident.create("+");
    Ident SUBTRACT_IDENT = Ident.create("-");

    static TypeEnv create() {
        return TypeEnvImpl.create();
    }

    static TypeEnv create(TypeEnv parent) {
        return TypeEnvImpl.create(parent);
    }

    static TypeEnv create(Map<Ident, PolyType> mappings) {
        return TypeEnvImpl.create(mappings);
    }

    static TypeEnv create(Map<Ident, PolyType> mappings, TypeEnv parent) {
        return TypeEnvImpl.create(mappings, parent);
    }

    static String toString(Map<Ident, PolyType> mappings) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<Ident, PolyType> assign : mappings.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(assign.getKey().toString());
            sb.append(": ");
            sb.append(assign.getValue().toString());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    void captureFreeVars(Set<VarType> freeVars);

    Set<VarType> freeVars();

    PolyType generalize(PolyType polyType);

    PolyType get(Ident ident);

    TypeEnv parent();

    void put(Ident ident, PolyType polyType);

    PolyType shallowGet(Ident ident);

    int shallowSize();

    int size();

    void subst(TypeSubst subst);
}

final class BuiltinTypeEnvImpl implements TypeEnv {

    static final BuiltinTypeEnvImpl SINGLETON = new BuiltinTypeEnvImpl();

    private final Map<Ident, PolyType> mappings;
    private final TypeEnv parent;

    private BuiltinTypeEnvImpl() {
        // CAUTION: There can be no free variables in builtin types.
        this.mappings = Map.of(
            Ident.create("Bool"), ScalarType.BOOL,
            Ident.create("Int32"), ScalarType.INT32,
            Ident.create("Int64"), ScalarType.INT64,
            Ident.create("Str"), ScalarType.STR,
            Ident.create("Void"), ScalarType.VOID,
            ADD_IDENT, QuantType.create(
                List.of(VarType.create("α")),
                FuncType.create(List.of(VarType.create("α"), VarType.create("α"), VarType.create("α")))
            ),
            SUBTRACT_IDENT, QuantType.create(
                List.of(VarType.create("α")),
                FuncType.create(List.of(VarType.create("α"), VarType.create("α"), VarType.create("α")))
            )
        );
        this.parent = null;
    }

    @Override
    public void captureFreeVars(Set<VarType> freeVars) {
    }

    @Override
    public final PolyType get(Ident ident) {
        return mappings.get(ident);
    }

    @Override
    public Set<VarType> freeVars() {
        return Set.of();
    }

    @Override
    public final PolyType generalize(PolyType polyType) {
        Set<VarType> polyFree = new HashSet<>(polyType.freeVars());
        return polyType.addQuantifiers(polyFree);
    }

    @Override
    public final PolyType shallowGet(Ident ident) {
        return mappings.get(ident);
    }

    @Override
    public final int shallowSize() {
        return mappings.size();
    }

    @Override
    public final int size() {
        return mappings.size();
    }

    @Override
    public final TypeEnv parent() {
        return parent;
    }

    @Override
    public final void put(Ident ident, PolyType polyType) {
        // This will never happen because BuiltinTypeEnvImpl must never be an inner scope.
        // In other words, BuiltinTypeEnvImpl is always the parent of another scope.
        throw new UnsupportedOperationException();
    }

    @Override
    public final void subst(TypeSubst subst) {
        // There are no free variables in builtin types
    }

    @Override
    public final String toString() {
        return TypeEnv.toString(mappings);
    }
}

final class TypeEnvImpl implements TypeEnv {

    private final Map<Ident, PolyType> mappings;
    private final TypeEnv parent;

    private TypeEnvImpl(Map<Ident, PolyType> mappings, TypeEnv parent) {
        this.mappings = mappings;
        this.parent = parent != null ? parent : BuiltinTypeEnvImpl.SINGLETON;
    }

    static TypeEnvImpl create() {
        return new TypeEnvImpl(new HashMap<>(), null);
    }

    static TypeEnvImpl create(TypeEnv parent) {
        return new TypeEnvImpl(new HashMap<>(), parent);
    }

    static TypeEnvImpl create(Map<Ident, PolyType> mappings) {
        return new TypeEnvImpl(new HashMap<>(mappings), null);
    }

    static TypeEnvImpl create(Map<Ident, PolyType> mappings, TypeEnv parent) {
        return new TypeEnvImpl(new HashMap<>(mappings), parent);
    }

    @Override
    public void captureFreeVars(Set<VarType> freeVars) {
        for (PolyType pt : mappings.values()) {
            pt.captureFreeVars(freeVars);
        }
        parent.captureFreeVars(freeVars);
    }

    @Override
    public final PolyType get(Ident ident) {
        PolyType answer = mappings.get(ident);
        if (answer == null) {
            answer = parent.get(ident);
        }
        return answer;
    }

    @Override
    public Set<VarType> freeVars() {
        Set<VarType> fvs = new HashSet<>();
        captureFreeVars(fvs);
        return fvs;
    }

    @Override
    public final PolyType generalize(PolyType polyType) {
        Set<VarType> polyFree = new HashSet<>(polyType.freeVars());
        polyFree.removeAll(freeVars());
        return polyType.addQuantifiers(polyFree);
    }

    @Override
    public final TypeEnv parent() {
        return parent;
    }

    @Override
    public final void put(Ident ident, PolyType polyType) {
        PolyType existing = mappings.putIfAbsent(ident, polyType);
        if (existing != null) {
            // TODO: This is AlreadyDefinedInScope
            throw new IllegalArgumentException("Duplicate identifier " + ident);
        }
    }

    @Override
    public final PolyType shallowGet(Ident ident) {
        return mappings.get(ident);
    }

    @Override
    public final int shallowSize() {
        return mappings.size();
    }

    @Override
    public final int size() {
        return parent.size() + mappings.size();
    }

    @Override
    public final void subst(TypeSubst subst) {
        parent.subst(subst);
        mappings.replaceAll((ident, polyType) -> polyType.subst(subst));
    }

    @Override
    public final String toString() {
        return TypeEnv.toString(mappings);
    }
}