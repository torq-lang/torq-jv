/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.local.Actor;

final class IntPublisherMod {

    private final CompleteRec moduleRec;

    private IntPublisherMod() {
        try {
            moduleRec = Rec.completeRecBuilder()
                .addField(Str.of("IntPublisher"), Actor.compileForImport(IntPublisher.SOURCE))
                .build();
        } catch (Exception exc) {
            throw new IllegalStateException("IntPublisherMod error", exc);
        }
    }

    public static CompleteRec moduleRec() {
        return LazySingleton.SINGLETON.moduleRec;
    }

    private static class LazySingleton {
        private static final IntPublisherMod SINGLETON = new IntPublisherMod();
    }

}
