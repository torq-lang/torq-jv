/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.ArrayList;
import java.util.List;

public class ActorCtor implements Proc {

    private final Closure handlersCtor;

    public ActorCtor(Closure handlersCtor) {
        this.handlersCtor = handlersCtor;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitActorCtor(this, state);
    }

    @Override
    public final void apply(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        // NOTE: An ActorCtor requires all arguments to be Complete. The last argument is not checked because it is
        // the return argument.
        List<Complete> resArgs = new ArrayList<>(ys.size());
        for (int i = 0; i < ys.size() - 1; i++) {
            CompleteOrIdent y = ys.get(i);
            Complete yRes = y.resolveValue(env).checkComplete();
            resArgs.add(yRes);
        }
        CompleteOrIdent target = ys.get(ys.size() - 1);
        ValueOrVar targetRes = target.resolveValueOrVar(env);
        ActorCfg actorCfg = new ActorCfg(resArgs, handlersCtor);
        targetRes.bindToValue(actorCfg, null);
    }

    public CompleteActorCtor checkComplete() throws WaitVarException {
        for (EnvEntry envEntry : handlersCtor.capturedEnv()) {
            envEntry.var.resolveValue().checkComplete();
        }
        return new CompleteActorCtor(handlersCtor);
    }

    public final Closure handlersCtor() {
        return handlersCtor;
    }

    @Override
    public final boolean isValidKey() {
        return true;
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
