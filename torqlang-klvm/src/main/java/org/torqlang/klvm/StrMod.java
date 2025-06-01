/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class StrMod implements KernelModule {

    public static final Str STR_STR = Str.of("Str");
    public static final Ident STR_IDENT = Ident.create(STR_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private StrMod() {
        namesake = new StrCls();
        exports = Rec.completeRecBuilder()
            .build();
    }

    public static StrMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     str.substring(start::Int32) -> Str
    //     str.substring(start::Int32, stop::Int32) -> Str
    static void objSubstring(Str obj, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        int argCount = ys.size();
        if (argCount < 2 || argCount > 3) {
            throw new InvalidArgCountError(2, 3, ys, "Str.substring");
        }
        Int64 beginIndex = (Int64) ys.get(0).resolveValue(env);
        Str subStr;
        ValueOrVar target;
        if (argCount == 2) {
            subStr = Str.of(obj.value.substring(beginIndex.intValue()));
            target = ys.get(1).resolveValueOrVar(env);
        } else {
            Int64 endIndex = (Int64) ys.get(1).resolveValue(env);
            subStr = Str.of(obj.value.substring(beginIndex.intValue(), endIndex.intValue()));
            target = ys.get(2).resolveValueOrVar(env);
        }
        target.bindToValueOrVar(subStr, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    @Override
    public final Complete namesake() {
        return namesake;
    }

    @Override
    public final Ident namesakeIdent() {
        return STR_IDENT;
    }

    private static final class LazySingleton {
        private static final StrMod SINGLETON = new StrMod();
    }

    static final class StrCls implements CompleteObj {

        private StrCls() {
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
