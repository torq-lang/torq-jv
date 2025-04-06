/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public final class SuffixFactory {

    private int nextAlphaSuffix = 1;
    private int nextBetaSuffix = 1;

    public final VarInfr nextAlphaVar() {
        return VarInfr.create(PolyInfr.LOWER_ALPHA + (nextAlphaSuffix++));
    }

    public final VarInfr nextBetaVar() {
        return VarInfr.create(PolyInfr.LOWER_BETA + (nextBetaSuffix++));
    }

}
