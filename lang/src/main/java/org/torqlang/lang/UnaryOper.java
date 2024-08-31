/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Map;

public enum UnaryOper {

    ACCESS("@"),
    NEGATE("-"),
    NOT("!");

    private static final Map<String, UnaryOper> symbolToValueMap = Map.of(
        ACCESS.symbol(), ACCESS,
        NEGATE.symbol(), NEGATE,
        NOT.symbol(), NOT
    );

    private final String symbol;

    UnaryOper(String symbol) {
        this.symbol = symbol;
    }

    public static UnaryOper valueForSymbol(String symbol) {
        return symbolToValueMap.get(symbol);
    }

    public final String symbol() {
        return symbol;
    }

}
