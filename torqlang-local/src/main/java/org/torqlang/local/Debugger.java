/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.util.List;
import java.util.function.Function;

public interface Debugger extends DebugInstrListener {
    void addRecognizer(Function<ActorRef, Boolean> recognizer);

    void onCreate(ActorRef caller, ActorSystem system, EnvEntry askHandlerEntry, EnvEntry tellHandlerEntry);

    void onFreeVarBound(ActorRef caller, Var triggerVar, Value value);

    void onMapFreeVar(ActorRef caller, Var triggerVar, Var parentVar, ActorRef child, Var childVar);

    void onPreempt(ActorRef caller, Machine machine);

    void onReceiveAct(ActorRef caller, Envelope request);

    void onReceiveCaptureImage(ActorRef caller, Envelope request);

    void onReceiveConfigure(ActorRef caller, Envelope request);

    void onReceiveNotify(ActorRef caller, Envelope notify, EnvEntry handlerEntry);

    void onReceiveRequest(ActorRef caller, Envelope request, EnvEntry handlerEntry);

    void onReceiveResponse(ActorRef caller, Envelope[] next, List<Envelope> allResponses);

    void onReceiveResume(ActorRef caller, Machine machine);

    void onReceiveStop(ActorRef caller, Machine machine);

    void onReceiveSyncFreeVar(ActorRef caller, Var var, Value value);

    void onSendResponse(ActorRef caller, ActorRef requester, Envelope request, Envelope response);

    void onSendSyncFreeVar(ActorRef caller, Var parentVar, Complete value, ActorRef child, Var childVar);

    void onWait(ActorRef caller, ComputeWait computeWait, Machine machine);

    void onWaitFreeVar(ActorRef caller, Var parentVar, Var childVar, Var barrier);

    void removeRecognizer(Function<ActorRef, Boolean> recognizer);
}
