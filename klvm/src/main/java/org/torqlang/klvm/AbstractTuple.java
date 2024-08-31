/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

abstract class AbstractTuple implements Tuple {

    @Override
    public final Int64 featureAt(int index) {
        if (index < 0 || index >= fieldCount()) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + fieldCount());
        }
        return Int32.of(index);
    }

    protected final int featureToIndex(Feature feature, int length) throws FeatureNotFoundError {
        if (!(feature instanceof Int64 int64)) {
            throw new FeatureNotFoundError(this, feature);
        }
        int index = int64.intValue();
        if (index < 0 || index >= length) {
            throw new FeatureNotFoundError(this, feature);
        }
        return index;
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
