/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;

public interface FeatureAsType extends ScalarAsType {

    static int compare(FeatureAsType a, FeatureAsType b) {
        return FeatureComparator.SINGLETON.compare(a.value(), b.value());
    }

    static FeatureAsType create(Feature feature) {
        if (feature instanceof Str str) {
            return StrAsType.create(str);
        }
        if (feature instanceof Int32 int32) {
            return Int32AsType.create(int32);
        }
        if (feature instanceof Bool bool) {
            return BoolAsType.create(bool);
        }
        if (feature instanceof Null) {
            return NullAsType.SINGLETON;
        }
        if (feature instanceof Eof) {
            return EofAsType.SINGLETON;
        }
        throw new IllegalArgumentException("Feature not valid as a type: " + feature);
    }

    Feature value();
}
