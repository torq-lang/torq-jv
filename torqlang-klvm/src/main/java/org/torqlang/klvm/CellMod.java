/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class CellMod implements KernelModule {

    public static final Str CELL_STR = Str.of("Cell");
    public static final Ident CELL_IDENT = Ident.create(CELL_STR.value);

    private final CompleteRec exports;

    private CellMod() {
        exports = Rec.completeRecBuilder()
            .addField(CELL_STR, CellCls.SINGLETON)
            .build();
    }

    public static Complete cellCls() {
        return CellCls.SINGLETON;
    }

    public static CellMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     new Cell(initial::Value) -> Cell
    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 2;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "Cell.new");
        }
        ValueOrVar initial = ys.get(0).resolveValueOrVar(env);
        CellObj cellObj = new CellObj(initial);
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValue(cellObj, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    static final class CellCls implements CompleteObj {
        private static final CellCls SINGLETON = new CellCls();
        private static final CompleteProc CELL_CLS_NEW = CellMod::clsNew;

        private CellCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.NEW)) {
                return CELL_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    static final class CellObj implements Obj {

        private static final ObjProcTable<CellObj> objProcTable = ObjProcTable.<CellObj>builder()
            .build();

        private ValueOrVar valueOrVar;

        CellObj(ValueOrVar valueOrVar) {
            this.valueOrVar = valueOrVar;
        }

        final ValueOrVar get() {
            return valueOrVar;
        }

        @Override
        public final boolean isValidKey() {
            return true;
        }

        @Override
        public final ValueOrVar select(Feature feature) {
            return objProcTable.selectAndBind(this, feature);
        }

        final ValueOrVar set(ValueOrVar valueOrVar) {
            ValueOrVar previous = this.valueOrVar;
            this.valueOrVar = valueOrVar;
            return previous;
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    private static final class LazySingleton {
        private static final CellMod SINGLETON = new CellMod();
    }

}
