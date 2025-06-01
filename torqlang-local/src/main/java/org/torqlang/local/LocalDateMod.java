/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.AbstractObjType;
import org.torqlang.lang.Type;
import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

final class LocalDateMod implements KernelModule {

    public static final Str LOCAL_DATE_STR = Str.of("LocalDate");
    public static final Ident LOCAL_DATE_IDENT = Ident.create(LOCAL_DATE_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private LocalDateMod() {
        namesake = new LocalDateCls();
        exports = Rec.completeRecBuilder()
            .addField(LOCAL_DATE_STR, namesake)
            .build();
    }

    public static LocalDateObj createObj(LocalDate date) {
        return new LocalDateObj(date);
    }

    // TODO: Make this a $TYPE feature on LocalDateCls
    public static Type localDateType() {
        return LocalDateType.SINGLETON;
    }

    public static LocalDateMod singleton() {
        return LazySingleton.SINGLETON;
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

    @Override
    public final Complete namesake() {
        return namesake;
    }

    @Override
    public final Ident namesakeIdent() {
        return LOCAL_DATE_IDENT;
    }

    private static final class LazySingleton {
        private static final LocalDateMod SINGLETON = new LocalDateMod();
    }

    static final class LocalDateCls implements CompleteObj {

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
        public final String formatAsKernelString() {
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

    private static final class LocalDateType extends AbstractObjType implements ValueTool {

        private static final LocalDateType SINGLETON = new LocalDateType(SourceSpan.emptySourceSpan());

        public LocalDateType(SourceSpan sourceSpan) {
            super(sourceSpan);
        }

        @Override
        public final Object toNativeValue(Complete value) {
            throw new NeedsImpl();
        }

        @Override
        public final Complete toKernelValue(Object value) {
            LocalDate localDate;
            if (value instanceof String string) {
                if (string.length() > 10) {
                    LocalDateTime localDateTime = DateTimeFormatter.ISO_DATE_TIME.parse(string, LocalDateTime::from);
                    localDate = localDateTime.toLocalDate();
                } else {
                    localDate = DateTimeFormatter.ISO_DATE.parse(string, LocalDate::from);
                }
            } else if (value instanceof LocalDate localDateFound) {
                localDate = localDateFound;
            } else {
                throw new IllegalArgumentException("Cannot convert value to LocalDate: " + value);
            }
            return LocalDateMod.createObj(localDate);
        }

        @Override
        public final Ident ident() {
            return LOCAL_DATE_IDENT;
        }
    }

}
