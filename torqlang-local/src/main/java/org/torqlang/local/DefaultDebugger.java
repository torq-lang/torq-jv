/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.util.StringTools;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Actor debug locations versus events:
 *
 * LocalActor --> onCreate
 * computeTimeSlice --> onWait, onPreempt
 * mapFreeVar --> onMapFreeVar
 * onMessage --> onReceiveResponse, onReceiveNotify, onReceiveRequest
 *     onControl
 *         onResume --> onReceiveResume
 *         onSyncFreeVar --> onReceiveSyncFreeVar
 *         onAct --> onReceiveAct
 *         onConfigure --> onReceiveConfigure
 *         onCaptureImage --> onReceiveCaptureImage
 *         onStop --> onReceiveStop
 * onFreeVarBound --> onFreeVarBound, onWaitFreeVar, onSendSyncFreeVar
 * onUnhandledError --> onSendResponse
 * sendResponse --> onSendResponse
 */
public final class DefaultDebugger implements Debugger {

    private static final String UNDEFINED = "undefined";

    private final Monitor monitor;

    private final List<Function<ActorRef, Boolean>> recognizers = Collections.synchronizedList(new ArrayList<>());
    private final Map<Object, Boolean> subjects = Collections.synchronizedMap(new WeakHashMap<>());

    public DefaultDebugger() {
        this(DefaultMonitor.SINGLETON);
    }

    public DefaultDebugger(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void addRecognizer(Function<ActorRef, Boolean> recognizer) {
        recognizers.add(recognizer);
    }

    public final void addSubject(Object subject) {
        subjects.put(subject, Boolean.TRUE);
    }

    private String formatSource(Instr instr) {
        return instr.formatSource("<<source location>>", 5, 25, 25);
    }

    private boolean isRecognized(ActorRef caller) {
        for (Function<ActorRef, Boolean> recognizer : recognizers) {
            if (recognizer.apply(caller)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSubject(ActorRef caller) {
        return subjects.containsKey(caller);
    }

    private boolean isSubject(Machine machine) {
        if (machine.owner() == null) {
            return false;
        }
        return subjects.containsKey(machine.owner());
    }

    private void monitor(Object... artifacts) {
        monitor.accept(artifacts);
    }

    @Override
    public final void onCreate(ActorRef caller,
                               ActorSystem system,
                               EnvEntry askHandlerEntry,
                               EnvEntry tellHandlerEntry)
    {
        if (isRecognized(caller)) {
            addSubject(caller);
        }
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, system, askHandlerEntry, tellHandlerEntry);
        }
    }

    @Override
    public final void onFreeVarBound(ActorRef caller, Var triggerVar, Value value) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, triggerVar, value);
        }
    }

    @Override
    public final void onMapFreeVar(ActorRef caller, Var triggerVar, Var parentVar, ActorRef child, Var childVar) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            String childAddress = child.address().toString();
            monitor(address, triggerVar, parentVar, childAddress, childAddress, childVar);
        }
    }

    @Override
    public final void onNextInstr(Instr nextInstr, Env nextEnv, Machine machine) {
        if (isSubject(machine)) {
            String address = ownerAddressOrName(machine);
            String formattedSource;
            if (!nextInstr.source().isEmpty()) {
                formattedSource = formatSource(nextInstr);
            } else {
                formattedSource = "<<source is empty>>";
            }
            String formattedInstr = new KernelFormatter(4).format(nextInstr);
            String sideBySide = StringTools.appendColumn(formattedInstr, formattedSource);
            monitor(address, sideBySide, machine);
        }
    }

    @Override
    public final void onPreempt(ActorRef caller, Machine machine) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            monitor(address, machine);
        }
    }

    @Override
    public final void onReceiveAct(ActorRef caller, Envelope request) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, request);
        }
    }

    @Override
    public final void onReceiveCaptureImage(ActorRef caller, Envelope request) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, request);
        }
    }

    @Override
    public final void onReceiveConfigure(ActorRef caller, Envelope request) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            monitor(address, request);
        }
    }

    @Override
    public final void onReceiveNotify(ActorRef caller, Envelope notify, EnvEntry handlerEntry) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            monitor(address, notify, handlerEntry);
        }
    }

    @Override
    public final void onReceiveRequest(ActorRef caller, Envelope request, EnvEntry handlerEntry) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            monitor(address, request, handlerEntry);
        }
    }

    @Override
    public final void onReceiveResponse(ActorRef caller, Envelope[] next, List<Envelope> allResponses) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, next, allResponses);
        }
    }

    @Override
    public final void onReceiveResume(ActorRef caller, Machine machine) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, machine);
        }
    }

    @Override
    public final void onReceiveStop(ActorRef caller, Machine machine) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, machine);
        }
    }

    @Override
    public void onReceiveSyncFreeVar(ActorRef caller, Var var, Value value) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, var, value);
        }
    }

    @Override
    public final void onSendResponse(ActorRef caller, ActorRef requester, Envelope request, Envelope response) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, requester, request, response);
        }
    }

    @Override
    public final void onSendSyncFreeVar(ActorRef caller, Var parentVar, Complete value, ActorRef child, Var childVar) {
        if (isSubject(caller)) {
            Address address = caller.address();
            monitor(address, parentVar, value, child, childVar);
        }
    }

    @Override
    public final void onWait(ActorRef caller, ComputeWait computeWait, Machine machine) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            String formattedSource;
            String idents;
            if (machine.stack() != null) {
                formattedSource = formatSource(machine.stack().instr);
                idents = machine.stack().env.collectIdents((Var) computeWait.barrier).stream()
                    .map(Ident::toString).collect(Collectors.joining(", ", "[", "]"));
            } else {
                formattedSource = "<<machine stack is empty>>";
                idents = "";
            }
            monitor(address, formattedSource, computeWait, idents);
        }
    }

    @Override
    public final void onWaitFreeVar(ActorRef caller, Var parentVar, Var childVar, Var barrier) {
        if (isSubject(caller)) {
            String address = caller.address().toString();
            monitor(address, parentVar, childVar, barrier);
        }
    }

    private String ownerAddressOrName(Machine machine) {
        if (machine.owner() == null) {
            return UNDEFINED;
        } else if (machine.owner() instanceof ActorRef actorRef) {
            return actorRef.address().toString();
        } else if (machine.owner() instanceof ActorBuilder actorBuilder) {
            return actorBuilder.address().toString();
        } else {
            return machine.owner().getClass().getName();
        }
    }

    @Override
    public void removeRecognizer(Function<ActorRef, Boolean> recognizer) {
        boolean removed;
        do {
            removed = recognizers.remove(recognizer);
        } while (removed);
    }

    public final void removeSubject(Address address) {
        subjects.remove(address);
    }

    public interface Monitor {
        void accept(Object... artifacts);
    }

    public static class DefaultMonitor implements Monitor {
        public static final DefaultMonitor SINGLETON = new DefaultMonitor();

        private DefaultMonitor() {
        }

        @Override
        public void accept(Object... artifacts) {
        }
    }
}
