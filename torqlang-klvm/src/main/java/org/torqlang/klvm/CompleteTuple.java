/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.*;

public interface CompleteTuple extends CompleteRec, Tuple {

    static CompleteTuple create(List<Complete> values) {
        return BasicCompleteTuple.createPrivatelyForKlvm(null, values.toArray(new Complete[0]));
    }

    static CompleteTuple create(Literal label, List<Complete> values) {
        return BasicCompleteTuple.createPrivatelyForKlvm(label, values.toArray(new Complete[0]));
    }

    static CompleteTuple singleton(Complete value) {
        return BasicCompleteTuple.createPrivatelyForKlvm(null, new Complete[]{value});
    }

    void addAllTo(Collection<? super Complete> collection);

    @Override
    default Object toNativeValue(IdentityHashMap<CompleteRec, Object> memos) {
        if (memos != null) {
            if (memos.containsKey(this)) {
                throw new IllegalArgumentException("Circular reference error");
            }
        } else {
            memos = new IdentityHashMap<>();
        }
        memos.put(this, Value.PRESENT);
        List<Object> values = new ArrayList<>(fieldCount());
        for (int i = 0; i < fieldCount(); i++) {
            Complete e = valueAt(i);
            Object v;
            if (e instanceof CompleteRec completeRec) {
                v = completeRec.toNativeValue(memos);
            } else {
                v = e.toNativeValue();
            }
            values.add(v);
        }
        if (label().equals(Rec.DEFAULT_LABEL)) {
            return values;
        }
        return Map.of($LABEL, label().toNativeValue(), $FIELDS, values);
    }

}
