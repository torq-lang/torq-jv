/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public class Int64Pack {

    public static final CompleteObj INT64_CLS = Int64Cls.SINGLETON;

    // Signatures:
    //     Int64.parse(num::Str) -> Int64
    static void clsParse(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 2;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "Int64.parse");
        }
        Str num = (Str) ys.get(0).resolveValue(env);
        Int64 int64 = Int64.of(Long.parseLong(num.value));
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValue(int64, null);
    }

    static class Int64Cls implements CompleteObj {
        private static final Int64Cls SINGLETON = new Int64Cls();
        private static final CompleteProc INT64_CLS_PARSE = Int64Pack::clsParse;

        private Int64Cls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.PARSE)) {
                return INT64_CLS_PARSE;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
