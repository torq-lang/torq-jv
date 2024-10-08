/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.SourceSpan;

public interface NumAsExpr extends ValueAsExpr {

    static Num parseAsFlt32OrFlt64OrDec128(String symbolText) {
        if (symbolText.endsWith("F") || symbolText.endsWith("f")) {
            return Flt32.of(symbolText.substring(0, symbolText.length() - 1));
        }
        if (symbolText.endsWith("D") || symbolText.endsWith("d")) {
            return Flt64.of(symbolText.substring(0, symbolText.length() - 1));
        }
        if (symbolText.endsWith("M") || symbolText.endsWith("m")) {
            return Dec128.decode(symbolText);
        }
        return Flt64.of(symbolText);
    }

    static Int64 parseAsInt32OrInt64(String symbolText) {
        if (symbolText.endsWith("L") || symbolText.endsWith("l")) {
            return Int64.decode(symbolText.substring(0, symbolText.length() - 1));
        }
        return Int32.decode(symbolText);
    }

    Num value();

}
