/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class CompleteActorCtor extends ActorCtor implements Complete {

    public CompleteActorCtor(Closure handlersCtor) {
        super(handlersCtor);
    }

    @Override
    public final CompleteActorCtor checkComplete() {
        return this;
    }

}
