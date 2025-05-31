/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class Int32Mod implements KernelModule {

    public static final Str INT32_STR = Str.of("Int32");
    public static final Ident INT32_IDENT = Ident.create(INT32_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private Int32Mod() {
        namesake = new Int32Cls();
        exports = Rec.completeRecBuilder()
            .addField(INT32_STR, namesake)
            .build();
    }

    public static Int32Mod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     Int32.parse(num::Str) -> Int32
    static void clsParse(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 2;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "Int32.parse");
        }
        Str num = (Str) ys.get(0).resolveValue(env);
        Int32 int32 = Int32.of(Integer.parseInt(num.value));
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValue(int32, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    @Override
    public final Complete namesake() {
        return namesake;
    }

    private static final class Int32Cls implements CompleteObj {

        private static final CompleteProc INT32_CLS_PARSE = Int32Mod::clsParse;

        private Int32Cls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.PARSE)) {
                return INT32_CLS_PARSE;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    private static final class LazySingleton {
        private static final Int32Mod SINGLETON = new Int32Mod();
    }

}
