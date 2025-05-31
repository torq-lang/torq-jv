/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.*;
import org.torqlang.local.Actor;

final class IntPublisherMod implements KernelModule {

    public static final Str INT_PUBLISHER_STR = Str.of("IntPublisher");
    public static final Ident INT_PUBLISHER_IDENT = Ident.create(INT_PUBLISHER_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private IntPublisherMod() {
        namesake = compileNamesake();
        exports = Rec.completeRecBuilder()
            .addField(INT_PUBLISHER_STR, namesake)
            .build();
    }

    private static CompleteRec compileNamesake() {
        try {
            Rec actorRec = Actor.builder()
                .setSource(IntPublisher.SOURCE)
                .construct()
                .actorRec();
            return (CompleteRec) actorRec.checkComplete();
        } catch (Exception exc) {
            throw new IllegalStateException("Error creating actor", exc);
        }
    }

    public static IntPublisherMod singleton() {
        return LazySingleton.SINGLETON;
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    @Override
    public final Complete namesake() {
        return namesake;
    }

    private static final class LazySingleton {
        private static final IntPublisherMod SINGLETON = new IntPublisherMod();
    }

}
