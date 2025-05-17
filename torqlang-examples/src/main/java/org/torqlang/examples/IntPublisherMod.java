/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.KernelModule;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.local.Actor;

final class IntPublisherMod implements KernelModule {

    private final CompleteRec exportsRec;

    private IntPublisherMod() {
        try {
            exportsRec = Rec.completeRecBuilder()
                .addField(Str.of("IntPublisher"), Actor.compileForImport(IntPublisher.SOURCE))
                .build();
        } catch (Exception exc) {
            throw new IllegalStateException("IntPublisherMod error", exc);
        }
    }

    public static IntPublisherMod singleton() {
        return LazySingleton.SINGLETON;
    }

    @Override
    public final CompleteRec exports() {
        return exportsRec;
    }

    private static final class LazySingleton {
        private static final IntPublisherMod SINGLETON = new IntPublisherMod();
    }

}
