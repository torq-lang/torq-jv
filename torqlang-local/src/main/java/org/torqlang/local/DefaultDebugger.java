/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DefaultDebugger implements Debugger {

    private final Logger logger;
    private final boolean trace;
    private final ConcurrentHashMap<ActorRef, String> subjects = new ConcurrentHashMap<>();

    public DefaultDebugger() {
        this(false, ConsoleLogger.SINGLETON);
    }

    public DefaultDebugger(boolean trace, Logger logger) {
        this.trace = trace;
        this.logger = logger;
    }

    public final void addSubject(ActorRef actorRef) {
        subjects.put(actorRef, actorRef.address().toString());
    }

    @Override
    public final void onActorAddParentVarDependency(ActorRef actorRef, Var triggerVar, Var parentVar, Var childVar, ActorRef child) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Adding bind dependency on var " + triggerVar + " to synchronize parent var " +
                    parentVar + " with child var " + childVar + " at child " + child.address();
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorCannotSyncWaitingToBind(ActorRef actorRef, Var parentVar, Var childVar, Var barrier) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String messageText = "Cannot synchronize parent var " + parentVar +
                    " to child var " + childVar + " because we are waiting to bind " + barrier;
                logger.info(messageText);
            }
        }
    }

    @Override
    public final void onActorComputeMessageUsingHandler(ActorRef actorRef, Value message, EnvEntry handlerEntry) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String messageText = "Computing message: " + message;
                logger.info(messageText);
            }
        }
    }

    @Override
    public final void onActorComputeTimeSlice(ActorRef actorRef, Machine machine) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Compute time slice at: " + actorRef.address();
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorComputeWait(ActorRef actorRef, ComputeWait computeWait, Machine machine) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String idents = machine.stack().env.collectIdents((Var) computeWait.barrier).stream()
                    .map(Ident::toString).collect(Collectors.joining(", ", "[", "]"));
                String label = "Waiting on " + computeWait.barrier + " with identifiers " + idents;
                String message = "Waiting on a variable\n" + machine.stack().stmt.formatSource(label, 5, 1, 2);
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorConfigure(ActorRef actorRef, Envelope envelope) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Configuring actor at: " + actorRef.address();
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorCreate(ActorRef actorRef,
                                    ActorSystem system,
                                    EnvEntry askHandlerEntry,
                                    EnvEntry tellHandlerEntry)
    {
        // TODO: Plug in and use a "recognizer" here that will add actors to the subjects list
        //       Add a debug event that will remove subjects at the end of life
        if (trace) {
            String message = "Actor created: " + actorRef.address();
            logger.info(message);
        }
    }

    @Override
    public final void onActorParentVarBound(ActorRef actorRef, Var triggerVar, Value value) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Parent var " + triggerVar + " bound with value " + value;
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorResponseReceived(ActorRef actorRef, Envelope response, Object target) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Received response for target: " + target + " with value: " + response.message();
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorRespondingWithValue(ActorRef actorRef, ActorRef requester, Envelope request, Envelope response) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Responding to " + requester.address() + " request message " + request.message() +
                    " with response message " + response;
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorResume(ActorRef actorRef) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Resuming actor: " + actorRef.address();
                logger.info(message);
            }
        }
    }

    @Override
    public final void onActorSyncParentVarToChildVar(ActorRef actorRef, Var parentVar, Var childVar, Complete value, ActorRef childRef) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Synchronizing from parent var " + parentVar +
                    " to child var " + childVar + " at child actor " + childRef.address() +
                    " with value: " + value;
                logger.info(message);
            }
        }
    }

    @Override
    public void onActorSyncVar(ActorRef actorRef, Var var, Value value) {
        if (subjects.containsKey(actorRef)) {
            if (trace) {
                String message = "Synchronizing var " + var + " with value: " + value;
                logger.info(message);
            }
        }
    }

    @Override
    public final void onDebugStmt(DebugStmt stmt, Machine machine) {
        if (subjects.containsKey((ActorRef) machine.owner())) {
            if (trace) {
                String message = stmt.nextStmt().formatSource("ON_DEBUG_STMT", 5, 1, 2);
                logger.info(message);
            }
        }
    }

    public final void removeSubject(ActorRef actorRef) {
        subjects.remove(actorRef);
    }
}
