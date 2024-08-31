/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Map;

public enum RelationalOper {

    EQUAL_TO("=="),
    NOT_EQUAL_TO("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">=");

    private static final Map<String, RelationalOper> symbolToValueMap = Map.of(
        EQUAL_TO.symbol(), EQUAL_TO,
        NOT_EQUAL_TO.symbol(), NOT_EQUAL_TO,
        LESS_THAN.symbol(), LESS_THAN,
        LESS_THAN_OR_EQUAL_TO.symbol(), LESS_THAN_OR_EQUAL_TO,
        GREATER_THAN.symbol(), GREATER_THAN,
        GREATER_THAN_OR_EQUAL_TO.symbol(), GREATER_THAN_OR_EQUAL_TO
    );

    private final String symbol;

    RelationalOper(String symbol) {
        this.symbol = symbol;
    }

    public static RelationalOper valueForSymbol(String symbol) {
        return symbolToValueMap.get(symbol);
    }

    public final String symbol() {
        return symbol;
    }

}
