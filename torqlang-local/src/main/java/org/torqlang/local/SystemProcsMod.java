/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

final class SystemProcsMod {
    static final CompleteRec moduleRec = createModuleRec();

    private static CompleteRec createModuleRec() {
        return Rec.completeRecBuilder()
            .addField(Str.of("actor_at"), (CompleteProc) LocalActor::onCallbackToActorAt)
            .addField(Str.of("assert_bound"), KernelProcs.ASSERT_BOUND_PROC)
            .addField(Str.of("is_bound"), KernelProcs.IS_BOUND_PROC)
            .addField(Str.of("is_det"), KernelProcs.IS_DET_PROC)
            .addField(Str.of("respond"), (CompleteProc) LocalActor::onCallbackToRespondFromProc)
            .build();
    }
}
