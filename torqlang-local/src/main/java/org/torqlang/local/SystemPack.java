/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

final class SystemPack {
    static final CompleteRec packageRec = createPack();

    private static CompleteRec createPack() {
        return Rec.completeRecBuilder()
            .addField(Str.of("ArrayList"), ArrayListMod.ARRAY_LIST_CLS)
            .addField(Str.of("Cell"), CellMod.CELL_CLS)
            .addField(Str.of("HashMap"), HashMapMod.HASH_MAP_CLS)
            .addField(Str.of("FieldIter"), FieldIterMod.FIELD_ITER_CLS)
            .addField(Str.of("Int32"), Int32Mod.INT32_CLS)
            .addField(Str.of("Int64"), Int64Mod.INT64_CLS)
            .addField(Str.of("LocalDate"), LocalDateMod.LOCAL_DATE_CLS)
            .addField(Str.of("RangeIter"), RangeIterMod.RANGE_ITER_CLS)
            .addField(Str.of("Rec"), RecMod.REC_CLS)
            .addField(Str.of("Stream"), LocalActor.StreamCls.SINGLETON)
            .addField(Str.of("Timer"), TimerMod.TIMER_ACTOR)
            .addField(Str.of("Token"), TokenMod.TOKEN_CLS)
            .addField(Str.of("ValueIter"), ValueIterMod.VALUE_ITER_CLS)
            .build();
    }
}
