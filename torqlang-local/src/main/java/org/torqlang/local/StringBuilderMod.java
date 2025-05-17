/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

final class StringBuilderMod implements KernelModule {

    public static final Str STRING_BUILDER_STR = Str.of("StringBuilder");
    public static final Ident STRING_BUILDER_IDENT = Ident.create(STRING_BUILDER_STR.value);

    private final CompleteRec exports;

    private StringBuilderMod() {
        exports = Rec.completeRecBuilder()
            .addField(STRING_BUILDER_STR, StringBuilderCls.SINGLETON)
            .build();
    }

    public static StringBuilderCls stringBuilderCls() {
        return StringBuilderCls.SINGLETON;
    }

    public static StringBuilderMod singleton() {
        return LazySingleton.SINGLETON;
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    private static final class LazySingleton {
        private static final StringBuilderMod SINGLETON = new StringBuilderMod();
    }

    static final class StringBuilderCls implements CompleteObj {
        private static final StringBuilderCls SINGLETON = new StringBuilderCls();

        private StringBuilderCls() {
        }

        @Override
        public final Value select(Feature feature) {
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
