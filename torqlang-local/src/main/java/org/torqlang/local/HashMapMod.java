/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.util.*;

final class HashMapMod implements KernelModule {

    public static final Str HASH_MAP_STR = Str.of("HashMap");
    public static final Ident HASH_MAP_IDENT = Ident.create(HASH_MAP_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private HashMapMod() {
        namesake = new HashMapCls();
        exports = Rec.completeRecBuilder()
            .addField(HASH_MAP_STR, namesake)
            .build();
    }

    public static HashMapMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     new HashMap() -> HashMap
    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 1;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "HashMap.new");
        }
        ValueOrVar y0 = ys.get(0).resolveValueOrVar(env);
        HashMapObj obj = new HashMapObj();
        y0.bindToValue(obj, null);
    }

    // Signatures:
    //     hash_map.get(key::Value) -> Value
    static void objGet(HashMapObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 2;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "HashMap.get");
        }
        Value candidateKey = ys.get(0).resolveValue(env);
        Complete key = candidateKey.checkComplete();
        if (!key.isValidKey()) {
            throw new NotValidKeyError(key);
        }
        ValueOrVar elem = obj.state.get(key);
        // An element not found results in the Null value
        if (elem == null) {
            elem = Null.SINGLETON;
        }
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        target.bindToValueOrVar(elem, null);
    }

    // Signatures:
    //     hash_map.put(key::Value, value::Value)
    static void objPut(HashMapObj obj, List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedCount = 2;
        if (ys.size() != expectedCount) {
            throw new InvalidArgCountError(expectedCount, ys, "HashMap.put");
        }
        Value candidateKey = ys.get(0).resolveValue(env);
        Complete key = candidateKey.checkComplete();
        if (!key.isValidKey()) {
            throw new NotValidKeyError(key);
        }
        ValueOrVar elem = ys.get(1).resolveValueOrVar(env);
        obj.state.put(key, elem);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    @Override
    public final Complete namesake() {
        return namesake;
    }

    static final class HashMapCls implements CompleteObj {

        private static final CompleteProc HASH_MAP_CLS_NEW = HashMapMod::clsNew;

        private HashMapCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.$NEW)) {
                return HASH_MAP_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    static final class HashMapObj implements Obj, FieldIterSource, ValueIterSource {

        private static final ObjProcTable<HashMapObj> objProcTable = ObjProcTable.<HashMapObj>builder()
            .addEntry(CommonFeatures.GET, HashMapMod::objGet)
            .addEntry(CommonFeatures.PUT, HashMapMod::objPut)
            .build();

        private final HashMap<Complete, ValueOrVar> state;

        HashMapObj() {
            state = new HashMap<>();
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
            HashMapObj that = (HashMapObj) other;
            return state.equals(that.state);
        }

        @Override
        public final ValueOrVar fieldIter() {
            return new MapFieldIter(state);
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

        public final HashMap<Complete, ValueOrVar> state() {
            return state;
        }

        @Override
        public final ValueOrVar valueIter() {
            return new MapValueIter(state);
        }

        static class MapFieldIter extends AbstractIter implements FieldIter {
            public MapFieldIter(HashMap<Complete, ValueOrVar> hashMap) {
                super(makeFields(hashMap));
            }

            private static List<Tuple> makeFields(HashMap<Complete, ValueOrVar> hashMap) {
                List<Tuple> tuples = new ArrayList<>();
                for (Map.Entry<Complete, ValueOrVar> entry : hashMap.entrySet()) {
                    tuples.add(PartialTuple.create(null, List.of(entry.getKey(), entry.getValue())));
                }
                return tuples;
            }
        }

        static class MapValueIter extends AbstractIter implements ValueIter {
            public MapValueIter(HashMap<Complete, ValueOrVar> hashMap) {
                super(hashMap.values());
            }
        }
    }

    private static final class LazySingleton {
        private static final HashMapMod SINGLETON = new HashMapMod();
    }

}
