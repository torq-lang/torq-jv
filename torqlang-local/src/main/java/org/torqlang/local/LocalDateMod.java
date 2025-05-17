/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

final class LocalDateMod implements KernelModule {

    public static final Str LOCAL_DATE_STR = Str.of("LocalDate");
    public static final Ident LOCAL_DATE_IDENT = Ident.create(LOCAL_DATE_STR.value);

    private final CompleteRec exports;

    private LocalDateMod() {
        exports = Rec.completeRecBuilder()
            .addField(LOCAL_DATE_STR, LocalDateCls.SINGLETON)
            .build();
    }

    public static Complete localDateCls() {
        return LocalDateCls.SINGLETON;
    }

    public static LocalDateMod singleton() {
        return LazySingleton.SINGLETON;
    }

    static LocalDateObj newObj(LocalDate date) {
        return new LocalDateObj(date);
    }

    // Signatures:
    //     LocalDate.parse(date::Str) -> LocalDate
    static void clsParse(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 2;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "LocalDate.parse");
        }
        Str dateStr = (Str) ys.get(0).resolveValue(env);
        LocalDateObj localDateObj = new LocalDateObj(LocalDate.parse(dateStr.value));
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValue(localDateObj, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    private static final class LazySingleton {
        private static final LocalDateMod SINGLETON = new LocalDateMod();
    }

    static final class LocalDateCls implements CompleteObj {
        private static final LocalDateCls SINGLETON = new LocalDateCls();
        private static final CompleteProc LOCAL_DATE_CLS_PARSE = LocalDateMod::clsParse;

        private LocalDateCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.PARSE)) {
                return LOCAL_DATE_CLS_PARSE;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    static final class LocalDateObj implements CompleteObj {

        private static final ObjProcTable<LocalDateObj> objProcTable = ObjProcTable.<LocalDateObj>builder()
            .build();

        private final LocalDate state;

        public LocalDateObj(LocalDate state) {
            this.state = state;
        }

        @Override
        public final boolean entails(Value operand, Set<Memo> memos) {
            return this.equals(operand);
        }

        @Override
        public final boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            LocalDateObj that = (LocalDateObj) other;
            return state.equals(that.state);
        }

        @Override
        public final String formatValue() {
            return state.toString();
        }

        @Override
        public final int hashCode() {
            return state.hashCode();
        }

        @Override
        public final boolean isValidKey() {
            return true;
        }

        @Override
        public final Value select(Feature feature) {
            throw new FeatureNotFoundError(this, feature);
        }

        public final LocalDate state() {
            return state;
        }

        @Override
        public final Object toNativeValue() {
            return state;
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
