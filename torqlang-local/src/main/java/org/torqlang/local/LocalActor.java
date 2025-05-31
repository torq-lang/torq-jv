/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.Stack;
import org.torqlang.klvm.*;
import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;

import java.util.*;

import static org.torqlang.local.OnMessageResult.FINISHED;
import static org.torqlang.local.OnMessageResult.NOT_FINISHED;
import static org.torqlang.util.ListTools.nullSafeCopyOf;
import static org.torqlang.util.SourceSpan.emptySourceSpan;

/*
 * Envelope Insertion -- An envelope is placed at the end of the mailbox queue unless the envelope is higher priority
 * than its predecessor. If the envelope is higher priority than its predecessor, their positions in the queue are
 * swapped. This repeats until the envelope is no longer higher priority than its predecessor.
 *
 * Wait State -- An actor is a single threaded kernel machine. The wait-state is implemented as a single field
 * holding the barrier value that suspended the machine. A non-null wait-state indicates we are waiting on a Response.
 * Otherwise, a null wait-state indicates we are waiting on a Notify or Request.
 *
 * Notify and Request messages are computation requests. Response messages affect the machines state, and Control
 * messages affect the actor lifecycle.
 *
 * Priority 0: Control message are the highest priority. Examples of control messages are Resume, Stop, and Debug.
 * Priority 1: Response messages are higher priority than request messages because the actor may be waiting on an
 *             unbound variable fulfilled by the response.
 * Priority 2: Request and notify message have the same priority, they are requesting that the actor perform a
 *             computation.
 *
 */
final class LocalActor extends AbstractActor {

    private static final Env ROOT_ENV = createRootEnv();

    private final ActorSystem system;
    private final IdentityHashMap<Var, List<ChildVar>> triggers = new IdentityHashMap<>();

    private boolean streamTrace = false;
    private Machine machine;
    private EnvEntry askHandlerEntry;
    private EnvEntry tellHandlerEntry;
    private Envelope activeRequest;
    private Object waitState;
    private int childCount;
    private FailedValue failedValue; // We are halted if not null

    private List<Envelope> selectableResponses = Collections.emptyList();
    private List<Envelope> suspendedResponses = Collections.emptyList();

    LocalActor(Address address, ActorImage image) {
        this(address, image.system, image.askHandlerEntry, image.tellHandlerEntry);
        machine = new Machine(this, null);
    }

    LocalActor(Address address, ActorSystem system) {
        this(address, system, null, null);
    }

    private LocalActor(Address address, ActorSystem system, EnvEntry askHandlerEntry, EnvEntry tellHandlerEntry) {
        super(address, system.createMailbox(), system.executor(), system.createLogger());
        this.system = system;
        this.askHandlerEntry = askHandlerEntry;
        this.tellHandlerEntry = tellHandlerEntry;
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onCreate(this, system, askHandlerEntry, tellHandlerEntry);
        }
    }

    private static Env createRootEnv() {
        List<EnvEntry> bindings = List.of(
            new EnvEntry(Ident.$ACT, new Var((CompleteProc) LocalActor::onCallbackToAct)),
            new EnvEntry(Ident.$IMPORT, new Var((CompleteProc) LocalActor::onCallbackToImport)),
            new EnvEntry(Ident.$RESPOND, new Var((CompleteProc) LocalActor::onCallbackToRespondFromAsk)),
            new EnvEntry(Ident.$SELF, new Var((CompleteProc) LocalActor::onCallbackToSelf)),
            new EnvEntry(Ident.$SPAWN, new Var((CompleteProc) LocalActor::onCallbackToSpawn))
        );
        return Env.create(bindings);
    }

    private static boolean nullSafeIsControl(Envelope envelope) {
        return envelope != null && envelope.isControl();
    }

    private static boolean nullSafeIsResponse(Envelope envelope) {
        return envelope != null && envelope.isResponse();
    }

    private static void onCallbackToAct(List<CompleteOrIdent> ys, Env env, Machine machine) {
        LocalActor owner = machine.owner();
        owner.performCallbackToAct(ys, env, machine);
    }

    static void onCallbackToActorAt(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        LocalActor owner = machine.owner();
        if (ys.size() != 2) {
            throw new InvalidArgCountError(2, ys, "LocalActor.onCallbackToActorAt");
        }
        Str addressStr = (Str) ys.get(0).resolveValue(env);
        Address address = Address.create(addressStr.value);
        ActorRefObj actorRefObj = owner.system.actorAt(address);
        ys.get(1).resolveValueOrVar(env).bindToValue(actorRefObj, null);
    }

    /*
     * Imports must be a type of `Complete`
     */
    static void onCallbackToImport(List<CompleteOrIdent> ys, Env env, Machine machine)
        throws WaitException
    {
        LocalActor owner = machine.owner();
        if (ys.size() != 2) {
            throw new InvalidArgCountError(2, ys, "LocalActor.onCallbackToImport");
        }
        Value qualifierRes = ys.get(0).resolveValue(env);
        if (!(qualifierRes instanceof Str qualifierStr)) {
            throw new IllegalArgumentException("Not a Str: " + qualifierRes);
        }
        String qualifier = qualifierStr.value;
        CompleteRec packageRec = owner.system.packageAt(qualifier);
        Value namesRes = ys.get(1).resolveValue(env);
        if (!(namesRes instanceof CompleteTuple namesTuple)) {
            throw new IllegalArgumentException("Not a CompleteTuple: " + namesRes);
        }
        for (int i = 0; i < namesTuple.fieldCount(); i++) {
            Str nameStr;
            Str aliasStr;
            Value nameValue = namesTuple.valueAt(i);
            if (nameValue instanceof CompleteTuple nameTuple) {
                nameStr = (Str) nameTuple.valueAt(0);
                aliasStr = (Str) nameTuple.valueAt(1);
            } else {
                nameStr = (Str) nameValue;
                aliasStr = nameStr;
            }
            Complete member = packageRec.findValue(nameStr);
            if (member == null) {
                throw new IllegalArgumentException("Member not found: " + nameStr);
            }
            Ident aliasIdent = Ident.create(aliasStr.value);
            env.get(aliasIdent).bindToValue(member, null);
        }
    }

    private static void onCallbackToRespondFromAsk(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        LocalActor owner = machine.owner();
        owner.sendResponse(ys, env, machine);
        // We are at the end of an ask-handler and have completed the request
        owner.activeRequest = null;
    }

    static void onCallbackToRespondFromProc(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        LocalActor owner = machine.owner();
        owner.sendResponse(ys, env, machine);
    }

    private static void onCallbackToSelf(List<CompleteOrIdent> ys, Env env, Machine machine) {
        LocalActor owner = machine.owner();
        owner.performCallbackToSelf(ys, env, machine);
    }

    private static void onCallbackToSpawn(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        LocalActor owner = machine.owner();
        owner.performCallbackToSpawn(ys, env, machine);
    }

    static Env rootEnv() {
        return ROOT_ENV;
    }

    static ActorRef spawn(Address address, ActorImage image) {
        return new LocalActor(address, image);
    }

    private void bindResponseValue(Envelope envelope) throws WaitException {

        // If the response is a typical request-response value, simply bind it.
        // Note that if the response is a FailedValue, it is bound here silently.

        if (envelope.requestId() instanceof ValueOrVarRef valueOrVarRef) {
            ValueOrVar responseTarget = valueOrVarRef.valueOrVar;
            Complete responseValue = (Complete) envelope.message();
            responseTarget.bindToValue(responseValue, null);
            return;
        }

        // Otherwise, the response is just one in a possible stream of values.

        StreamObjRef streamObjRef = (StreamObjRef) envelope.requestId();
        StreamObj streamObj = streamObjRef.streamObj;

        // Unlike a typical request-response, we need to check for a FailedValue
        // and bind it explicitly.

        if (envelope.message() instanceof FailedValue childFailedValue) {
            streamObj.tail.element.bindToValue(childFailedValue, null);
            streamObj.appendUnboundTail();
            return;
        }

        CompleteRec messageRec = (CompleteRec) envelope.message();

        // Although inefficient, it's legal for a publisher to return an empty batch of values, and
        // in that case, we have nothing to bind and nothing else to do.
        if (messageRec.fieldCount() == 0) {
            return;
        }

        // An 'eof' response must have a 'more' feature.
        if (messageRec.label().equals(Eof.SINGLETON)) {
            Bool more = (Bool) messageRec.valueAt(0);
            if (more.value) {
                streamObj.fetchNextFromPublisher();
            } else {
                streamObj.tail.element.bindToValue(Eof.SINGLETON, null);
            }
            return;
        }

        // At this point, we know we have a tuple of values. We must bind the first
        // value to the current tail Var. The remaining values are appended to the
        // stream.
        CompleteTuple values = (CompleteTuple) messageRec;
        Complete responseValue = values.valueAt(0);
        streamObj.tail.element.bindToValue(responseValue, null);
        streamObj.appendRemainingResponseValues(values);
    }

    private ComputeAdvice computeTimeSlice() {
        // Compute only returns a halt in response to two conditions:
        //     1. Compute touched a FailedValue
        //         (a) The halt contains the touched (remote) FailedValue but the local stack
        //     2. Compute threw an exception that was not caught
        //         (a) An indirect throw can originate when:
        //             1. A native Java program throws a NativeThrow
        //                 (a) The resulting NativeThrowError contains an 'error' value
        //                 (b) A 'throw error' instruction is pushed and run
        //                 (c) The halt will contain an error and the native throw error
        //             2. Compute catches a generic Throwable
        //                 (a) The throwable is rerun as a 'throw error#{name: _, ...}' instruction
        //                 (b) The halt will contain a NativeError
        //         (b) The halt contains the uncaught throw and local stack
        // If we are in the middle of an active request (activeRequest != null), we
        // must convert the halt into a FailedValue.
        //     1. If compute touched a FailedValue
        //         (a) Create a new FailedValue with the given FailedValue as its cause
        //     2. If compute threw an exception that was not caught
        //         (a) Create a FailedValue with an error and native cause
        //         (b) Native error should be "'error'#{'name': _, 'message': _, ...}"
        waitState = null;
        ComputeAdvice advice = machine.compute(10_000);
        if (advice.isWait()) {
            ComputeWait computeWait = (ComputeWait) advice;
            if (DebuggerSetting.get() != null) {
                DebuggerSetting.get().onWait(this, computeWait, machine);
            }
            waitState = computeWait.barrier;
        } else if (advice.isPreempt()) {
            if (DebuggerSetting.get() != null) {
                DebuggerSetting.get().onPreempt(this, machine);
            }
            send(Resume.SINGLETON);
        } else if (advice.isHalt()) {
            throw new MachineHaltError((ComputeHalt) advice);
        }
        return advice;
    }

    private ComputeAdvice computeTimeSlice(Value message, EnvEntry handlerEntry) {
        if (machine.stack() != null) {
            throw new IllegalStateException("Previous computation is not finished");
        }
        EnvEntry messageEntry = new EnvEntry(Ident.$NEXT, new Var(message));
        Env computeEnv = Env.create(Env.emptyEnv(), handlerEntry, messageEntry);
        SourceSpan sourceSpan = getHandlerSourceSpan(handlerEntry);
        ApplyInstr computeInstr = new ApplyInstr(Ident.$HANDLER, Collections.singletonList(Ident.$NEXT), sourceSpan);
        if (DebuggerSetting.get() != null) {
            DebugInstr debugInstr = new DebugInstr(DebuggerSetting.get(), computeInstr, computeEnv, computeInstr);
            machine.pushStackEntry(debugInstr, Env.emptyEnv());
        } else {
            machine.pushStackEntry(computeInstr, computeEnv);
        }
        return computeTimeSlice();
    }

    final void configure(ActorCfg actorCfg) {
        send(Envelope.createControlNotify(new Configure(actorCfg)));
    }

    private SourceSpan getHandlerSourceSpan(EnvEntry handlerEntry) {
        Closure handlerClosure = (Closure) handlerEntry.var.valueOrVarSet();
        return handlerClosure.sourceSpan();
    }

    @Override
    protected final boolean isExecutable(Mailbox mailbox) {
        if (waitState != null) {
            Envelope next = mailbox.peek();
            return nullSafeIsResponse(next) || !selectableResponses.isEmpty() || nullSafeIsControl(next);
        }
        return !mailbox.isEmpty();
    }

    private void mapFreeVar(Var triggerVar, Var parentVar, Var childVar, LocalActor child) {
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onMapFreeVar(this, triggerVar, parentVar, child, childVar);
        }
        List<ChildVar> childVars = triggers.get(triggerVar);
        if (childVars == null) {
            childVars = new ArrayList<>();
            triggers.put(triggerVar, childVars);
            triggerVar.setBindCallback(this::onFreeVarBound);
        }
        childVars.add(new ChildVar(parentVar, childVar, child));
    }

    private LocalAddress nextChildAddress() {
        childCount++;
        return LocalAddress.create(address(), Integer.toString(childCount));
    }

    private OnMessageResult onAct(Envelope envelope) {
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onReceiveAct(this, envelope);
        }
        activeRequest = envelope;
        Act act = (Act) envelope.message();
        Env actEnv = Env.create(ROOT_ENV, act.input);
        actEnv = actEnv.add(new EnvEntry(act.target, new Var()));
        machine = new Machine(LocalActor.this, new Stack(act.seq, actEnv, null));
        computeTimeSlice();
        return NOT_FINISHED;
    }

    private OnMessageResult onCaptureImage(Envelope envelope) {

        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onReceiveCaptureImage(this, envelope);
        }

        activeRequest = envelope;

        if (!triggers.isEmpty()) {
            throw new IllegalStateException("Triggers exist");
        }
        if (machine.stack() != null) {
            throw new IllegalStateException("Machine stack exists");
        }
        try {
            ((Value) askHandlerEntry.var.valueOrVarSet()).checkComplete();
        } catch (WaitException exc) {
            throw new IllegalStateException("Ask handler is not complete");
        }
        try {
            ((Value) tellHandlerEntry.var.valueOrVarSet()).checkComplete();
        } catch (WaitException exc) {
            throw new IllegalStateException("Tell handler is not complete");
        }
        if (waitState != null) {
            throw new IllegalStateException("Wait state is present");
        }
        if (childCount != 0) {
            throw new IllegalStateException("Child count is not zero");
        }
        if (failedValue != null) {
            throw new IllegalStateException("Actor is failed");
        }
        if (!selectableResponses.isEmpty()) {
            throw new IllegalStateException("Selectable responses are present");
        }
        if (!suspendedResponses.isEmpty()) {
            throw new IllegalStateException("Suspended responses are present");
        }

        ActorImage image = new ActorImage(system, askHandlerEntry, tellHandlerEntry);
        envelope.requester().send(Envelope.createResponse(image, envelope.requestId()));

        // An actor that serves its image is complete after serving.
        return FINISHED;
    }

    private OnMessageResult onConfigure(Envelope envelope) {
        boolean debuggerIsActive = DebuggerSetting.get() != null;
        if (debuggerIsActive) {
            DebuggerSetting.get().onReceiveConfigure(this, envelope);
        }

        // Extract the actor configuration from the incoming Configure message
        Configure configure = (Configure) envelope.message();
        ActorCfg actorCfg = configure.actorCfg;

        // Create the kernel machine and necessary environment to construct the handlers
        machine = new Machine(LocalActor.this, null);
        List<EnvEntry> envEntries = new ArrayList<>();
        EnvEntry handlersEntry = new EnvEntry(Ident.$HANDLERS, new Var());
        envEntries.add(handlersEntry);

        // Build a list of arguments for the handlers constructor
        List<Complete> args = actorCfg.args();
        List<CompleteOrIdent> argIdents = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            Ident argIdent = Ident.createSystemArgIdent(i);
            argIdents.add(argIdent);
            envEntries.add(new EnvEntry(argIdent, new Var(args.get(i))));
        }
        argIdents.add(Ident.$HANDLERS);

        // Compute the handlers
        Var constructorVar = new Var(actorCfg.handlersCtor());
        envEntries.add(new EnvEntry(Ident.$HANDLERS_CTOR, constructorVar));
        Env configEnv = Env.create(ROOT_ENV, envEntries);
        ApplyInstr computeInstr = new ApplyInstr(Ident.$HANDLERS_CTOR, argIdents, emptySourceSpan());
        if (debuggerIsActive) {
            DebugInstr debugInstr = new DebugInstr(DebuggerSetting.get(), computeInstr, configEnv, computeInstr);
            machine.pushStackEntry(debugInstr, Env.emptyEnv());
        } else {
            machine.pushStackEntry(computeInstr, configEnv);
        }
        ComputeAdvice advice = computeTimeSlice();
        if (advice != ComputeEnd.SINGLETON) {
            throw new IllegalStateException("Did not compute handlers");
        }
        if (!(handlersEntry.var.valueOrVarSet() instanceof Tuple handlers)) {
            throw new IllegalStateException("Handlers is not a Tuple");
        }

        // Save the `ask` handler and `tell` handler separately
        Closure askClosure = (Closure) handlers.valueAt(0);
        askHandlerEntry = new EnvEntry(Ident.$HANDLER, new Var(askClosure));
        Closure tellClosure = (Closure) handlers.valueAt(1);
        tellHandlerEntry = new EnvEntry(Ident.$HANDLER, new Var(tellClosure));

        return NOT_FINISHED;
    }

    private OnMessageResult onControl(Envelope envelope) {
        if (envelope == Resume.SINGLETON) {
            return onResume();
        }
        if (envelope.isResponse()) {
            throw new IllegalArgumentException("Invalid control response");
        }
        if (envelope.message() instanceof SyncFreeVar syncFreeVar) {
            return onSyncFreeVar(syncFreeVar);
        }
        if (envelope.message() instanceof Act) {
            return onAct(envelope);
        }
        if (envelope.message() instanceof Configure) {
            return onConfigure(envelope);
        }
        if (envelope.message() instanceof CaptureImage) {
            return onCaptureImage(envelope);
        }
        if (envelope.message() == Stop.SINGLETON) {
            return onStop(envelope);
        }
        throw new IllegalArgumentException("Invalid control message: " + envelope);
    }

    /*
     * An original mapping may begin as P -> (P, P') where binding P synchronizes P and P'. However, if P is bound to
     * a partial record {feature: X}, as an example, then the mapping P -> (P, P') is replaced with a new mapping
     * X -> (P, P') where binding X synchronizes P with P'. This process is iterative in cases where P is bound to a
     * compound partial record, such as {name: X, address: Y}. Two possible binding sequences in this example are
     * P -> (P, P'), X -> (P, P'), Y -> (P, P'); and P -> (P, P'), Y -> (P, P'), X -> (P, P'). If all or some of P's
     * components are bound before P itself is bound, then the binding sequence may be zero or one binding,
     * respectively.
     */
    private void onFreeVarBound(Var triggerVar, Value value) {
        boolean isDebuggerActive = DebuggerSetting.get() != null;
        if (isDebuggerActive) {
            DebuggerSetting.get().onFreeVarBound(this, triggerVar, value);
        }
        List<ChildVar> childVars = triggers.remove(triggerVar);
        if (childVars != null) {
            for (ChildVar childVar : childVars) {
                Complete parentComplete;
                try {
                    // Resolve parent var as a complete value
                    parentComplete = childVar.parentVar.resolveValueOrVar().checkComplete();
                } catch (WaitVarException wx) {
                    if (isDebuggerActive) {
                        DebuggerSetting.get().onWaitFreeVar(this, childVar.parentVar, childVar.childVar, wx.barrier());
                    }
                    // The parentVar is not yet complete. Therefore, we need to create a new trigger to try again
                    // when the next part of parentVar is completed.
                    Var nextTriggerVar = wx.barrier();
                    List<ChildVar> nextChildVars = triggers.get(nextTriggerVar);
                    if (nextChildVars == null) {
                        nextChildVars = childVars;
                        triggers.put(nextTriggerVar, nextChildVars);
                        nextTriggerVar.setBindCallback(this::onFreeVarBound);
                    } else {
                        nextChildVars.addAll(childVars);
                    }
                    return;
                }
                if (isDebuggerActive) {
                    DebuggerSetting.get().onSendSyncFreeVar(this, childVar.parentVar,
                        parentComplete, childVar.child, childVar.childVar);
                }
                childVar.child.send(Envelope.createControlNotify(new SyncFreeVar(childVar.childVar, parentComplete)));
            }
        }
    }

    @Override
    protected final OnMessageResult onMessage(Envelope[] next) {
        // It's possible to be executable with zero incoming response messages because we have a collection of
        // selectableResponses and optionally a collection of suspendedResponses.
        if (next.length == 0 || next[0].isResponse()) {
            List<Envelope> waitingResponses = new ArrayList<>();
            List<Envelope> allResponses = new ArrayList<>(next.length + selectableResponses.size());
            Collections.addAll(allResponses, next);
            allResponses.addAll(selectableResponses);
            allResponses.addAll(suspendedResponses);
            if (DebuggerSetting.get() != null) {
                DebuggerSetting.get().onReceiveResponse(this, next, allResponses);
            }
            for (Envelope envelope : allResponses) {
                try {
                    bindResponseValue(envelope);
                } catch (WaitException exc) {
                    waitingResponses.add(envelope);
                }
            }
            if (waitingResponses.size() == allResponses.size()) {
                // All responses failed to bind. Therefore, we leave waitState as-is and none of the responses are
                // selectable until we are able to bind a new response.
                suspendedResponses = waitingResponses;
                selectableResponses = Collections.emptyList();
                return NOT_FINISHED;
            }
            // Otherwise, we were able to bind some responses. We will now move waitingResponses to selectableResponses
            // and remain executable. It may take multiple passes to bind all responses because of responses depending
            // on other responses to complete.
            suspendedResponses = Collections.emptyList();
            selectableResponses = waitingResponses;
            computeTimeSlice();
        } else {
            if (next.length != 1) {
                throw new IllegalArgumentException("Not a single envelope");
            }
            Envelope only = next[0];
            if (only.isControl()) {
                return onControl(only);
            }
            if (only.isNotify()) {
                if (DebuggerSetting.get() != null) {
                    DebuggerSetting.get().onReceiveNotify(this, only, tellHandlerEntry);
                }
                computeTimeSlice((Value) only.message(), tellHandlerEntry);
                return NOT_FINISHED;
            }
            // We know we have a request
            activeRequest = only;
            if (DebuggerSetting.get() != null) {
                DebuggerSetting.get().onReceiveRequest(this, only, askHandlerEntry);
            }
            computeTimeSlice((Value) only.message(), askHandlerEntry);
        }
        return NOT_FINISHED;
    }

    protected final void onReceivedAfterFailed(Envelope envelope) {
        if (envelope.isRequest()) {
            envelope.requester().send(Envelope.createResponse(failedValue, envelope.requestId()));
        } else {
            super.onReceivedAfterFailed(envelope);
        }
    }

    private OnMessageResult onResume() {
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onReceiveResume(this, machine);
        }
        computeTimeSlice();
        return NOT_FINISHED;
    }

    private OnMessageResult onStop(Envelope envelope) {
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onReceiveStop(this, machine);
        }
        if (envelope.requester() != null) {
            envelope.requester().send(Envelope.createControlResponse(Stop.SINGLETON, envelope.requestId()));
        }
        return FINISHED;
    }

    /*
     * SyncFreeVar is used to synchronizes free variables mapped from a parent actor to a child actor. Free variables
     * are mapped when an `act ... end` expression is lifted from a parent actor and passed to a child actor to be
     * executed. When a free variable becomes bound in the parent actor, it sends a `SyncFreeVar` message to the child
     * so it can continue and complete its execution.
     */
    private OnMessageResult onSyncFreeVar(SyncFreeVar syncFreeVar) {
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onReceiveSyncFreeVar(this, syncFreeVar.var, syncFreeVar.value);
        }
        try {
            syncFreeVar.var.bindToValue(syncFreeVar.value, null);
        } catch (WaitException exc) {
            throw new IllegalStateException("Received WaitException binding a SyncFreeVar message");
        }
        computeTimeSlice();
        return NOT_FINISHED;
    }

    @Override
    protected final void onUnhandledError(Mailbox mailbox, Throwable throwable) {
        // CREATE FAILED VALUE
        if (throwable instanceof MachineHaltError machineHaltError) {
            ComputeHalt computeHalt = machineHaltError.computeHalt();
            if (computeHalt.touchedFailedValue != null) {
                // This means the halt occurred in another actor and the machine threw a FailedValueError
                failedValue = new FailedValue(address().toString(), computeHalt.touchedFailedValue.error(),
                    computeHalt.current, computeHalt.touchedFailedValue, computeHalt.nativeCause);
            } else {
                // This means the halt occurred in this actor and the machine threw an UncaughtThrowError
                failedValue = new FailedValue(address().toString(), computeHalt.uncaughtThrow,
                    computeHalt.current, null, computeHalt.nativeCause);
            }
        } else {
            failedValue = FailedValue.create(address().toString(), machine.stack(), throwable);
        }
        // RESPOND TO ACTIVE REQUEST
        if (activeRequest != null) {
            Envelope response = Envelope.createResponse(failedValue, activeRequest.requestId());
            if (DebuggerSetting.get() != null) {
                DebuggerSetting.get().onSendResponse(this, activeRequest.requester(),
                    activeRequest, response);
            }
            activeRequest.requester().send(response);
        } else {
            String errorText = "Actor halted\n" + failedValue.toDetailsString();
            logError(errorText);
        }
        // EMPTY THE MAILBOX WHILE RESPONDING TO REQUESTS
        while (!mailbox.isEmpty()) {
            Envelope next = mailbox.remove();
            if (next.isRequest()) {
                next.requester().send(Envelope.createResponse(failedValue, next.requestId()));
            }
        }
    }

    private void performCallbackToAct(List<CompleteOrIdent> ys, Env env, Machine machine) {

        LocalActor child = new LocalActor(nextChildAddress(), system);

        Instr current = machine.current().instr;
        ActInstr actInstr;
        if (current instanceof DebugInstr debugInstr) {
            actInstr = (ActInstr) debugInstr.nextInstr();
        } else {
            actInstr = (ActInstr) machine.current().instr;
        }

        HashSet<Ident> lexicallyFree = new HashSet<>();
        actInstr.captureLexicallyFree(new HashSet<>(), lexicallyFree);

        // Map free unbound parent variables to new child variables. When mapped variables are bound in the parent,
        // a SyncFreeVar message will be sent to the child.

        List<EnvEntry> childInput = new ArrayList<>();
        for (Ident freeIdent : lexicallyFree) {
            if (ROOT_ENV.contains(freeIdent) || freeIdent.equals(actInstr.target)) {
                continue;
            }
            Var parentVar = env.get(freeIdent);
            ValueOrVar valueOrVar = parentVar.resolveValueOrVar();
            Var childVar;
            if (valueOrVar instanceof Var) {
                childVar = new Var();
                mapFreeVar(parentVar, parentVar, childVar, child);
            } else {
                try {
                    childVar = new Var(valueOrVar.checkComplete());
                } catch (WaitVarException wx) {
                    childVar = new Var();
                    mapFreeVar(wx.barrier(), parentVar, childVar, child);
                }
            }
            childInput.add(new EnvEntry(freeIdent, childVar));
        }

        // Lift the act instruction from the parent and spawn it using a child actor

        ArrayList<Instr> instrList = new ArrayList<>(2);
        instrList.add(actInstr.instr);
        instrList.add(new ApplyInstr(Ident.$RESPOND, List.of(actInstr.target), actInstr.sourceSpan.toSourceEnd()));
        SeqInstr seq = new SeqInstr(instrList, actInstr.sourceSpan);
        ValueOrVar responseTarget = actInstr.target.resolveValueOrVar(env);
        Act act = new Act(seq, actInstr.target, childInput);
        child.send(Envelope.createControlRequest(act, LocalActor.this, new ValueOrVarRef(responseTarget)));
    }

    private void performCallbackToSelf(List<CompleteOrIdent> ys, Env env, Machine machine) {
        throw new NeedsImpl();
    }

    private void performCallbackToSpawn(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        if (ys.size() != 2) {
            throw new InvalidArgCountError(2, ys, "LocalActor.onCallbackToSpawn");
        }
        // Let's resolve the target early in case there is a variable error. We do not want to spawn an actor only to
        // find out that the variable is not found.
        ValueOrVar target = ys.get(1).resolveValueOrVar(env);
        Value config = ys.get(0).resolveValue(env);
        ActorRefObj childRefObj;
        if (config instanceof ActorCfg actorCfg) {
            childRefObj = spawnActorCfg(actorCfg);
        } else {
            childRefObj = spawnNativeActorCfg((NativeActorCfg) config);
        }
        target.bindToValue(childRefObj, null);
    }

    protected final Envelope[] selectNext(Mailbox mailbox) {
        Envelope first = mailbox.remove();
        if (first == null) {
            // Although there are no messages in the mailbox, we are executable because we contain selectable
            // responses. Therefore, return an empty batch of envelopes. (see isExecutable)
            return new Envelope[0];
        }
        if (!first.isResponse()) {
            return new Envelope[]{first};
        }
        ArrayList<Envelope> responses = new ArrayList<>();
        responses.add(first);
        Envelope nextEnvelope = mailbox.peek();
        while (nextEnvelope != null && nextEnvelope.isResponse()) {
            responses.add(mailbox.remove());
            nextEnvelope = mailbox.peek();
        }
        return responses.toArray(new Envelope[0]);
    }

    private void sendResponse(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitVarException {
        if (ys.size() != 1) {
            throw new InvalidArgCountError(1, ys, "LocalActor.sendResponse");
        }
        Value candidateValue = ys.get(0).resolveValue(env);
        // Check complete accomplishes the following:
        //   1. If candidate value is not completable, throw a CannotConvertToComplete error
        //   2. If candidate value is complete, we can progress and the value will be sent
        //   3. If candidate value is partial, a WaitException is thrown
        Complete responseValue = candidateValue.checkComplete();
        // Check for the subtle case where a 'respond' expression simply returns the result of an 'ask'
        // expression. For example, consider the following 'query' handler that returns the result of a call
        // to 'OrderDaoRef.ask(findOrder#{orderId: Id})':
        //     respond query#{orderId: Id::Str} in
        //         OrderDaoRef.ask(findOrder#{orderId: Id})
        //     end
        // Instead of simply returning the child FailedValue, we want to return a FailedValue chain that includes
        // the parent.
        if (responseValue instanceof FailedValue childFailedValue) {
            responseValue = new FailedValue(address().toString(), childFailedValue.error(),
                machine.current(), childFailedValue, null);
        }
        Envelope response = Envelope.createResponse(responseValue, activeRequest.requestId());
        if (DebuggerSetting.get() != null) {
            DebuggerSetting.get().onSendResponse(this, activeRequest.requester(),
                activeRequest, response);
        }
        activeRequest.requester().send(response);
    }

    private ActorRefObj spawnActorCfg(ActorCfg parentCfg) throws WaitException {

        // Only complete values are shared across process boundaries.
        // Note that createRootEnv() only creates CompleteProc values.
        HashMap<Ident, Complete> childCapturedEnvMap = new HashMap<>();
        for (EnvEntry rootEntry : ROOT_ENV) {
            childCapturedEnvMap.put(rootEntry.ident, (Complete) rootEntry.var.valueOrVarSet());
        }

        // The parent ActorCfg may contain partial values until used to spawn a child actor. Checking completeness as
        // late as possible can increase the opportunity to run in parallel. Here, as we transport the configuration
        // from the parent process to the child process, so we must ensure that the configuration is complete, or that
        // we can suspend until it becomes complete.
        Closure parentHandlersCtor = parentCfg.handlersCtor();
        Env parentCapturedEnv = parentHandlersCtor.capturedEnv();
        for (EnvEntry parentEntry : parentCapturedEnv) {
            // Root env entries have already been added.
            if (ROOT_ENV.contains(parentEntry.ident)) {
                continue;
            }
            ValueOrVar parentValueOrVar = parentEntry.var.resolveValueOrVar();
            childCapturedEnvMap.put(parentEntry.ident, parentValueOrVar.checkComplete());
        }
        CompleteClosure childHandlersCtor = new CompleteClosure(parentHandlersCtor.procDef(), childCapturedEnvMap);
        Configure configure = new Configure(new ActorCfg(parentCfg.args(), childHandlersCtor));
        LocalActor childActor = new LocalActor(nextChildAddress(), system);

        // We have a complete configuration and can now configure a concurrent actor process.
        childActor.send(Envelope.createControlNotify(configure));
        return new ActorRefObj(childActor);
    }

    private ActorRefObj spawnNativeActorCfg(NativeActorCfg nativeActorCfg) {
        ActorRef actorRef = nativeActorCfg.spawn(nextChildAddress(), system);
        return new ActorRefObj(actorRef);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + address() + ")";
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class Act {
        private final SeqInstr seq;
        private final Ident target;
        private final List<EnvEntry> input;

        private Act(SeqInstr seq, Ident target, List<EnvEntry> input) {
            this.seq = seq;
            this.target = target;
            this.input = nullSafeCopyOf(input);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class ChildVar {
        private final Var parentVar;
        private final Var childVar;
        private final LocalActor child;

        private ChildVar(Var parentVar, Var childVar, LocalActor child) {
            this.parentVar = parentVar;
            this.childVar = childVar;
            this.child = child;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class Configure {
        private final ActorCfg actorCfg;

        private Configure(ActorCfg actorCfg) {
            this.actorCfg = actorCfg;
        }
    }

    private static final class Resume implements Envelope {
        private static final Resume SINGLETON = new Resume();

        private Resume() {
        }

        @Override
        public final boolean isControl() {
            return true;
        }

        @Override
        public final Object message() {
            return Null.SINGLETON;
        }

        @Override
        public final Object requestId() {
            return null;
        }

        @Override
        public final ActorRef requester() {
            return null;
        }
    }

    static final class StreamCls implements CompleteObj {
        static final StreamCls SINGLETON = new StreamCls();

        private static final CompleteProc STREAM_CLS_NEW = StreamCls::clsNew;

        private StreamCls() {
        }

        private static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
            final int expectedCount = 3;
            if (ys.size() != expectedCount) {
                throw new InvalidArgCountError(expectedCount, ys, "LocalActor.Stream.new");
            }
            ActorRefObj publisher = (ActorRefObj) ys.get(0).resolveValue(env);
            Complete requestMessage = (Complete) ys.get(1).resolveValue(env);
            StreamObj streamObj = new StreamObj(machine.owner(), publisher, requestMessage);
            ValueOrVar target = ys.get(2).resolveValueOrVar(env);
            target.bindToValue(streamObj, null);
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.$NEW)) {
                return STREAM_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

    private static final class StreamEntry {

        private final ValueOrVar element;
        private StreamEntry nextEntry;

        private StreamEntry() {
            this.element = new Var();
            this.nextEntry = null;
        }

        private StreamEntry(Complete element) {
            this.element = element;
            this.nextEntry = null;
        }

        private void setNextEntry(StreamEntry nextEntry) {
            if (this.nextEntry != null) {
                throw new IllegalStateException("Next entry is already set");
            }
            this.nextEntry = nextEntry;
        }

    }

    private static class StreamIter implements ValueIter {

        private final LocalActor localActor;
        private final StreamObj streamObj;

        private boolean waiting = false;

        private StreamIter(LocalActor localActor, StreamObj streamObj) {
            this.streamObj = streamObj;
            this.localActor = localActor;
        }

        @Override
        public void apply(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {

            if (localActor.streamTrace) {
                localActor.logInfo("StreamIter performing an iteration");
            }

            if (ys.size() != VALUE_ITER_ARG_COUNT) {
                throw new InvalidArgCountError(VALUE_ITER_ARG_COUNT, ys, "LocalActor.StreamIter()");
            }

            ValueOrVar headValueOrVar = streamObj.head.element.resolveValueOrVar();

            if (waiting) {
                if (headValueOrVar instanceof Var var) {
                    if (localActor.streamTrace) {
                        localActor.logInfo("StreamIter cannot iterate because we are already waiting, throwing a WaitVarException");
                    }
                    throw new WaitVarException(var);
                }
                waiting = false;
                streamObj.head = streamObj.head.nextEntry;
                headValueOrVar = streamObj.head.element.resolveValueOrVar();
            }

            if (headValueOrVar instanceof Var var) {
                if (localActor.streamTrace) {
                    localActor.logInfo("StreamIter binding unbound stream head " + var + " to identifier " + ys.get(0));
                }
                ValueOrVar y = ys.get(0).resolveValueOrVar(env);
                var.bindToValueOrVar(y, null);
                waiting = true;
                return;
            }

            Complete headValue = (Complete) headValueOrVar;
            ValueOrVar y = ys.get(0).resolveValueOrVar(env);
            if (localActor.streamTrace) {
                localActor.logInfo("StreamIter binding next value " + headValue + " to iterator variable " + y);
            }
            y.bindToValue(headValue, null);
            if (headValue != Eof.SINGLETON) {
                streamObj.head = streamObj.head.nextEntry;
            }
        }

    }

    private static final class StreamObj implements Obj, ValueIterSource {
        private final LocalActor localActor;
        private final ActorRefObj publisher;
        private final RequestId requestId;
        private final Complete requestMessage;
        private final StreamIter streamIter;

        private StreamEntry head = new StreamEntry();
        private StreamEntry tail = head;

        private StreamObj(LocalActor localActor, ActorRefObj publisher, Complete requestMessage) {
            this.localActor = localActor;
            this.publisher = publisher;
            this.requestId = new StreamObjRef(this);
            this.requestMessage = requestMessage;
            this.streamIter = new StreamIter(localActor, this);
            fetchNextFromPublisher();
        }

        private void appendRemainingResponseValues(CompleteTuple values) {
            for (int i = 1; i < values.fieldCount(); i++) {
                Complete appendValue = values.valueAt(i);
                if (localActor.streamTrace) {
                    localActor.logInfo("StreamObj appending response to stream tail: " + appendValue);
                }
                StreamEntry newTail = new StreamEntry(appendValue);
                tail.setNextEntry(newTail);
                tail = newTail;
            }
            appendUnboundTail();
        }

        private void appendUnboundTail() {
            StreamEntry unboundTail = new StreamEntry();
            tail.setNextEntry(unboundTail);
            tail = unboundTail;
        }

        private void fetchNextFromPublisher() {
            if (localActor.streamTrace) {
                localActor.logInfo("StreamObj sending request " + requestMessage + " to " + publisher.referent().address());
            }
            publisher.referent().send(Envelope.createRequest(requestMessage, localActor, requestId));
            if (localActor.streamTrace) {
                localActor.logInfo("StreamObj request " + requestMessage + " sent to " + publisher.referent().address());
            }
        }

        @Override
        public final ValueOrVar select(Feature feature) {
            throw new NeedsImpl();
        }

        @Override
        public final ValueOrVar valueIter() {
            return streamIter;
        }
    }

    /*
     * This wrapper exists so a stream object can be exchanged across actor boundaries as a request ID without risk it
     * will be modified beyond its owner.
     */
    private static final class StreamObjRef extends OpaqueValue implements RequestId {
        private final StreamObj streamObj;

        private StreamObjRef(StreamObj streamObj) {
            this.streamObj = streamObj;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class SyncFreeVar {
        private final Var var;
        private final Complete value;

        private SyncFreeVar(Var var, Complete value) {
            this.var = var;
            this.value = value;
        }
    }

}
