/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

final class SystemLangPackage implements KernelPackage {

    private final CompleteRec packageRec;

    private SystemLangPackage() {
        packageRec = Rec.completeRecBuilder()
            .addField(Str.of("actor_at"), (CompleteProc) LocalActor::onCallbackToActorAt)
            .addField(Str.of("assert_bound"), KernelProcs.ASSERT_BOUND_PROC)
            .addField(Str.of("is_bound"), KernelProcs.IS_BOUND_PROC)
            .addField(Str.of("is_det"), KernelProcs.IS_DET_PROC)
            .addField(Str.of("respond"), (CompleteProc) LocalActor::onCallbackToRespondFromProc)
            .addAllFields(CellMod.singleton().exports())
            .addAllFields(FieldIterMod.singleton().exports())
            .addAllFields(Int32Mod.singleton().exports())
            .addAllFields(Int64Mod.singleton().exports())
            .addAllFields(RangeIterMod.singleton().exports())
            .addAllFields(RecMod.singleton().exports())
            .addField(Str.of("Stream"), LocalActor.StreamCls.SINGLETON)
            .addAllFields(TokenMod.singleton().exports())
            .addAllFields(ValueIterMod.singleton().exports())
            .build();
    }

    public static SystemLangPackage singleton() {
        return LazySingleton.SINGLETON;
    }

    @Override
    public final CompleteRec packageRec() {
        return packageRec;
    }

    @Override
    public final String path() {
        return "system.lang";
    }

    private static final class LazySingleton {
        private static final SystemLangPackage SINGLETON = new SystemLangPackage();
    }

}
