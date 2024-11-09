/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

public interface UnionDesc extends ValueDesc {

    // TODO: Add a discriminator scheme:
    //     Discriminated record unions:
    //         {(feature, value) -> RecDesc, ..., (feature, value) -> RecDesc}
    //     Other value unions:
    //         ScalarDesc, TupleDesc, RecDesc, <<specific-values>>

}
