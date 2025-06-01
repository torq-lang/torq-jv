/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class ArrayListMod implements KernelModule {

    public static final Str ARRAY_LIST_STR = Str.of("ArrayList");
    public static final Ident ARRAY_LIST_IDENT = Ident.create(ARRAY_LIST_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private ArrayListMod() {
        namesake = new ArrayListCls();
        exports = Rec.completeRecBuilder()
            .addField(ARRAY_LIST_STR, namesake)
            .build();
    }

    public static ArrayListMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     new ArrayList() -> ArrayList
    //     new ArrayList(values::Array) -> ArrayList
    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        int argCount = ys.size();
        if (argCount < 1 || argCount > 2) {
            throw new InvalidArgCountError(1, 2, ys, "ArrayList.new");
        }
        ArrayListObj arrayListObj;
        if (argCount == 1) {
            arrayListObj = new ArrayListObj();
        } else {
            Value v = ys.get(0).resolveValue(env);
            if (!(v instanceof Rec r)) {
                throw new IllegalArgumentException("Initial argument must be a record");
            }
            r.checkDetermined();
            int fieldCount = r.fieldCount();
            ArrayList<ValueOrVar> elements = new ArrayList<>(fieldCount);
            if (r instanceof Tuple tuple) {
                for (int i = 0; i < fieldCount; i++) {
                    elements.add(tuple.valueAt(i));
                }
            } else {
                for (int i = 0; i < fieldCount; i++) {
                    Field f = r.fieldAt(i);
                    if (!f.feature().resolveValue().equals(Int32.of(i))) {
                        throw new IllegalArgumentException("Not an array");
                    }
                    elements.add(f.value());
                }
            }
            arrayListObj = new ArrayListObj(elements);
        }
        ValueOrVar target = ys.get(argCount - 1).resolveValueOrVar(env);
        target.bindToValue(arrayListObj, null);
    }

    // Signatures:
    //     array_list.add(element::Value)
    static void objAdd(ArrayListObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) {
        final int expectedArgCount = 1;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "ArrayList.add");
        }
        ValueOrVar elem = ys.get(0).resolveValueOrVar(env);
        obj.state.add(elem);
    }

    // Signatures:
    //     array_list.clear()
    static void objClear(ArrayListObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) {
        final int expectedArgCount = 0;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "ArrayList.clear");
        }
        obj.state.clear();
    }

    // Signatures:
    //     array_list.size() -> Int32
    static void objSize(ArrayListObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 1;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "ArrayList.size");
        }
        ValueOrVar target = ys.get(0).resolveValueOrVar(env);
        target.bindToValueOrVar(Int32.of(obj.state.size()), null);
    }

    // Signatures:
    //     array_list.to_array() -> Array
    static void objToArray(ArrayListObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 1;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "ArrayList.to_array");
        }
        PartialTupleBuilder builder = Rec.partialTupleBuilder();
        for (ValueOrVar elem : obj.state) {
            builder.addValue(elem);
        }
        Tuple tuple = builder.build();
        ValueOrVar target = ys.get(0).resolveValueOrVar(env);
        target.bindToValueOrVar(tuple, null);
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
        return ARRAY_LIST_IDENT;
    }

    static final class ArrayListCls implements CompleteObj {

        private static final CompleteProc ARRAY_LIST_CLS_NEW = ArrayListMod::clsNew;

        private ArrayListCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.$NEW)) {
                return ARRAY_LIST_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    static final class ArrayListObj implements Obj, ValueIterSource {

        private static final ObjProcTable<ArrayListObj> objProcTable = ObjProcTable.<ArrayListObj>builder()
            .addEntry(CommonFeatures.ADD, ArrayListMod::objAdd)
            .addEntry(CommonFeatures.CLEAR, ArrayListMod::objClear)
            .addEntry(CommonFeatures.SIZE, ArrayListMod::objSize)
            .addEntry(CommonFeatures.TO_ARRAY, ArrayListMod::objToArray)
            .build();

        final ArrayList<ValueOrVar> state;

        public ArrayListObj() {
            state = new ArrayList<>();
        }

        public ArrayListObj(List<ValueOrVar> elements) {
            state = new ArrayList<>(elements);
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
            ArrayListObj that = (ArrayListObj) other;
            return this.state.equals(that.state);
        }

        @Override
        public final int hashCode() {
            return state.hashCode();
        }

        @Override
        public final boolean isValidKey() {
            return false;
        }

        @Override
        public final Value select(Feature feature) {
            return objProcTable.selectAndBind(this, feature);
        }

        final ArrayList<ValueOrVar> state() {
            return state;
        }

        @Override
        public final String toString() {
            return toKernelString();
        }

        @Override
        public final ValueOrVar valueIter() {
            return new ListValueIter(state);
        }

        static class ListValueIter extends AbstractIter implements ValueIter {
            public ListValueIter(List<ValueOrVar> values) {
                super(values);
            }
        }
    }

    private static final class LazySingleton {
        private static final ArrayListMod SINGLETON = new ArrayListMod();
    }

}
