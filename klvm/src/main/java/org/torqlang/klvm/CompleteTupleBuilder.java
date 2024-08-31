/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.ArrayList;
import java.util.List;

public final class CompleteTupleBuilder {

    private final List<Complete> values = new ArrayList<>();
    private Literal label;

    CompleteTupleBuilder() {
    }

    public final CompleteTupleBuilder addValue(Complete value) {
        values.add(value);
        return this;
    }

    public final CompleteTuple build() {
        return BasicCompleteTuple.createPrivatelyForKlvm(label, values.toArray(new Complete[0]));
    }

    public final CompleteTupleBuilder setLabel(Literal label) {
        this.label = label;
        return this;
    }

}
