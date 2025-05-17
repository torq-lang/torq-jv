/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class RangeIterMod implements KernelModule {

    public static final Str RANGE_ITER_STR = Str.of("RangeIter");
    public static final Ident RANGE_ITER_IDENT = Ident.create(RANGE_ITER_STR.value);

    private final CompleteRec exports;

    private RangeIterMod() {
        exports = Rec.completeRecBuilder()
            .addField(RANGE_ITER_STR, RangeIterCls.SINGLETON)
            .build();
    }

    public static Complete rangeIterCls() {
        return RangeIterCls.SINGLETON;
    }

    public static RangeIterMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     RangeIter.new(from::Int32, to::Int32) -> RangeIter
    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 3;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "RangeIter.new");
        }
        // RangeIter is not suspendable. Therefore, its arguments must be bound before we construct it.
        Int64 fromInt = (Int64) ys.get(0).resolveValue(env);
        Int64 toInt = (Int64) ys.get(1).resolveValue(env);
        RangeIterObj rangeIter = new RangeIterObj(fromInt, toInt);
        ValueOrVar target = ys.get(2).resolveValueOrVar(env);
        target.bindToValue(rangeIter, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    private static final class LazySingleton {
        private static final RangeIterMod SINGLETON = new RangeIterMod();
    }

    static final class RangeIterCls implements CompleteObj {
        private static final RangeIterCls SINGLETON = new RangeIterCls();
        private static final CompleteProc RANGE_ITER_CLS_NEW = RangeIterMod::clsNew;

        private RangeIterCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.NEW)) {
                return RANGE_ITER_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    static final class RangeIterObj implements Proc {

        private final static int RANGE_ITER_ARG_COUNT = 1;

        private final Int64 fromInt;
        private final Int64 toInt;
        private Int64 nextInt;

        public RangeIterObj(Int64 fromInt, Int64 toInt) {
            this.fromInt = fromInt;
            this.toInt = toInt;
            this.nextInt = fromInt;
        }

        @Override
        public final void apply(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
            if (ys.size() != RANGE_ITER_ARG_COUNT) {
                throw new InvalidArgCountError(RANGE_ITER_ARG_COUNT, ys, this);
            }
            ValueOrVar y = ys.get(0).resolveValueOrVar(env);
            if (nextInt.compareValueTo(toInt) < 0) {
                y.bindToValue(nextInt, null);
                nextInt = (Int64) nextInt.addFrom(Int32.I32_1);
            } else {
                y.bindToValue(Eof.SINGLETON, null);
            }
        }

        @Override
        public final boolean isValidKey() {
            return true;
        }

        @Override
        public final String toString() {
            return toKernelString();
        }

    }
}
