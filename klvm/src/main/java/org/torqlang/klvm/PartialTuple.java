/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public interface PartialTuple extends PartialRec, Tuple {

    static PartialTuple create(LiteralOrVar label, List<ValueOrVar> values) {
        ValueOrVar[] valuesArray = new ValueOrVar[values.size()];
        values.toArray(valuesArray);
        return BasicPartialTuple.createPrivatelyForKlvm(label, valuesArray);
    }

}
