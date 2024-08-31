/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

/**
 * Iterate fields (feature-value pairs) in an order defined by the underlying source.
 */
public interface FieldIter extends Proc {

    int FIELD_ITER_ARG_COUNT = 1;

}
