/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class RecMod implements KernelModule {

    public static final Str REC_STR = Str.of("Rec");
    public static final Ident REC_IDENT = Ident.create(REC_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private RecMod() {
        namesake = new RecCls();
        exports = Rec.completeRecBuilder()
            .addField(REC_STR, namesake)
            .build();
    }

    public static RecMod singleton() {
        return LazySingleton.SINGLETON;
    }

    /*
     * Rec.assign(from::Rec, to::Rec) -> Rec
     *
     * Assign fields from the left argument to the right argument, which effectively adds or replaces fields on the
     * right side.
     *
     * var from = { b: 4, c: 5 }
     * var to = { a: 1, b: 2 }
     *
     * Rec.assign(from, to, result) // result = { a: 1, b: 4, c: 5 }
     */
    static void clsAssign(RecCls cls, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 3;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "Rec.assign");
        }
        PartialRecBuilder builder = Rec.partialRecBuilder();
        Rec rec0 = (Rec) ys.get(0).resolveValue(env);
        rec0.checkDetermined();
        for (int i = 0; i < rec0.fieldCount(); i++) {
            builder.addField(rec0.featureAt(i), rec0.valueAt(i));
        }
        Rec rec1 = (Rec) ys.get(1).resolveValue(env);
        rec1.checkDetermined();
        for (int i = 0; i < rec1.fieldCount(); i++) {
            Feature rec1Feat = rec1.featureAt(i);
            if (rec0.findValue(rec1Feat) == null) {
                builder.addField(rec1Feat, rec1.valueAt(i));
            }
        }
        Rec assigned = builder.build();
        ValueOrVar target = ys.get(2).resolveValueOrVar(env);
        target.bindToValue(assigned, null);
    }

    // Signatures:
    //    Rec.size(rec::Rec) -> Int32
    static void clsSize(RecCls cls, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 2;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "Rec.size");
        }
        Rec rec0 = (Rec) ys.get(0).resolveValue(env);
        rec0.checkDetermined();
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValue(Int32.of(rec0.fieldCount()), null);
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
        return REC_IDENT;
    }

    private static final class LazySingleton {
        private static final RecMod SINGLETON = new RecMod();
    }

    private static final class RecCls implements CompleteObj {

        private static final ObjProcTable<RecCls> clsProcTable = ObjProcTable.<RecCls>builder()
            .addEntry(CommonFeatures.ASSIGN, RecMod::clsAssign)
            .addEntry(CommonFeatures.SIZE, RecMod::clsSize)
            .build();

        private RecCls() {
        }

        @Override
        public final Value select(Feature feature) {
            return clsProcTable.selectAndBind(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
