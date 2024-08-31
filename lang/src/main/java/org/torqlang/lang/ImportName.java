/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Str;

public final class ImportName {
    public final Str name;
    public final Str alias;

    public ImportName(Str name, Str alias) {
        this.name = name;
        this.alias = alias;
    }

    public ImportName(Str name) {
        this.name = name;
        this.alias = null;
    }
}
