/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

@SuppressWarnings("ClassCanBeRecord")
public class CompleteField implements DeterminedField {

    public final Feature feature;
    public final Complete value;

    public CompleteField(Feature feature, Complete value) {
        this.feature = feature;
        this.value = value;
    }

    @Override
    public final Feature feature() {
        return feature;
    }

    @Override
    public final Complete value() {
        return value;
    }

}
