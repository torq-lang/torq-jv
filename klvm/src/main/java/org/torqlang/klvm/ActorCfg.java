/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class ActorCfg implements Obj {

    private final List<Complete> args;
    private final Closure handlersCtor;

    public ActorCfg(List<Complete> args, Closure handlersCtor) {
        this.args = args;
        this.handlersCtor = handlersCtor;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitActorCfg(this, state);
    }

    public final List<Complete> args() {
        return args;
    }

    public final Closure handlersCtor() {
        return handlersCtor;
    }

    public final ValueOrVar select(Feature feature) throws WaitException {
        throw new FeatureNotFoundError(this, feature);
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
