/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

//TODO: Add recognizers

public interface Debugger extends DebugStmtListener {
    void onActorAddParentVarDependency(ActorRef actorRef, Var triggerVar, Var parentVar, Var childVar, ActorRef child);

    void onActorCannotSyncWaitingToBind(ActorRef actorRef, Var parentVar, Var childVar, Var barrier);

    void onActorComputeMessageUsingHandler(ActorRef actorRef, Value message, EnvEntry handlerEntry);

    void onActorComputeTimeSlice(ActorRef actorRef, Machine machine);

    void onActorComputeWait(ActorRef actorRef, ComputeWait computeWait, Machine machine);

    void onActorConfigure(ActorRef actorRef, Envelope envelope);

    void onActorCreate(ActorRef actorRef, ActorSystem system, EnvEntry askHandlerEntry, EnvEntry tellHandlerEntry);

    void onActorParentVarBound(ActorRef actorRef, Var triggerVar, Value value);

    void onActorResponseReceived(ActorRef actorRef, Envelope response, Object target);

    void onActorRespondingWithValue(ActorRef actorRef, ActorRef requester, Envelope request, Envelope response);

    void onActorResume(ActorRef actorRef);

    void onActorSyncParentVarToChildVar(ActorRef actorRef, Var parentVar, Var childVar, Complete value, ActorRef childRef);

    void onActorSyncVar(ActorRef actorRef, Var var, Value value);
}
