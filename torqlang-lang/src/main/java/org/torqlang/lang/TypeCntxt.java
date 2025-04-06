/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TypeCntxt {

    private static final TypeCntxt EMPTY_CNTXT = new TypeCntxt();

    private final Map<Lang, PolyInfr> mappings;

    private TypeCntxt() {
        this(Collections.emptyMap());
    }

    private TypeCntxt(Map<Lang, PolyInfr> mappings) {
        this.mappings = mappings;
    }

    public static TypeCntxt create(Map<Lang, PolyInfr> mappings) {
        return new TypeCntxt(Map.copyOf(mappings));
    }

    public static TypeCntxt empty() {
        return EMPTY_CNTXT;
    }

    public Set<VarInfr> freeVars() {
        Set<VarInfr> fvs = new HashSet<>();
        for (PolyInfr pt : mappings.values()) {
            pt.captureFreeVars(fvs);
        }
        return fvs;
    }

    public final PolyInfr generalize(PolyInfr polyType) {
        Set<VarInfr> polyFree = new HashSet<>(polyType.freeVars());
        polyFree.removeAll(freeVars());
        return polyType.addQuantifiers(polyFree);
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<Lang, PolyInfr> assign : mappings.entrySet()) {
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
}
