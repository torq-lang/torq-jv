/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.KernelPackage;
import org.torqlang.klvm.Rec;

final class SystemUtilPackage implements KernelPackage {

    private final CompleteRec packageRec;

    private SystemUtilPackage() {
        packageRec = Rec.completeRecBuilder()
            .addAllFields(ArrayListMod.singleton().exports())
            .addAllFields(HashMapMod.singleton().exports())
            .addAllFields(LocalDateMod.singleton().exports())
            .addAllFields(TimerMod.singleton().exports())
            .build();
    }

    public static SystemUtilPackage singleton() {
        return LazySingleton.SINGLETON;
    }

    @Override
    public final CompleteRec packageRec() {
        return packageRec;
    }

    @Override
    public final String path() {
        return "system.util";
    }

    private static final class LazySingleton {
        private static final SystemUtilPackage SINGLETON = new SystemUtilPackage();
    }

}
