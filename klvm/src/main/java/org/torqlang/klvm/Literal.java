/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Set;

public interface Literal extends Obj, Feature, LiteralOrVar, LiteralOrIdent, LiteralOrIdentPtn {

    @Override
    default Literal bindToValue(Value value, Set<Memo> memos) {
        if (!this.equals(value)) {
            throw new UnificationError(this, value);
        }
        return this;
    }

    @Override
    default ValueOrVar select(Feature feature) {
        throw new FeatureNotFoundError(this, feature);
    }

}
