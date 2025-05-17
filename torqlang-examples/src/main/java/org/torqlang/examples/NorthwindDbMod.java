/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.*;
import org.torqlang.local.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public final class NorthwindDbMod implements KernelModule {

    public static final Str NORTHWIND_DB_STR = Str.of("NorthwindDb");
    public static final Ident NORTHWIND_DB_IDENT = Ident.create(NORTHWIND_DB_STR.value);

    private final CompleteRec exports;

    private NorthwindDbMod() {
        exports = Rec.completeRecBuilder()
            .addField(NORTHWIND_DB_STR, NorthwindDbCls.SINGLETON)
            .build();
    }

    public static NorthwindDb northwindDb() {
        return NorthwindDbCls.NORTHWIND_DB;
    }

    public static NorthwindDbCls northwindDbCls() {
        return NorthwindDbCls.SINGLETON;
    }

    public static Executor northwindDbExecutor() {
        return NorthwindDbCls.NORTHWIND_DB_EXECUTOR;
    }

    public static NorthwindDbMod singleton() {
        return LazySingleton.SINGLETON;
    }

    private static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        if (ys.size() != NorthwindDbCls.NORTHWIND_DB_CTOR_ARG_COUNT) {
            throw new InvalidArgCountError(NorthwindDbCls.NORTHWIND_DB_CTOR_ARG_COUNT, ys, "NorthwindDb.new");
        }
        NorthwindDbCfg config = new NorthwindDbCfg();
        ys.get(0).resolveValueOrVar(env).bindToValue(config, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    private static final class LazySingleton {
        private static final NorthwindDbMod SINGLETON = new NorthwindDbMod();
    }

    /*
     * Using an ActorRef as a protocol adapter is a repeating pattern.
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static final class NorthwindDbAdapter implements ActorRef {

        private final Address address;

        NorthwindDbAdapter(Address address) {
            this.address = address;
        }

        @Override
        public Address address() {
            return address;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void send(Envelope envelope) {
            if (envelope.isRequest()) {
                CompleteRec message = (CompleteRec) envelope.message();
                if (message.label().equals(NorthwindDbCls.FIND_ALL_STR)) {
                    Str entity = (Str) message.findValue(NorthwindDbCls.ENTITY_STR);
                    CompleteRec criteria = (CompleteRec) message.findValue(NorthwindDbCls.CRITERIA_STR);
                    Map<String, Object> criteriaValue = null;
                    if (criteria.fieldCount() > 0) {
                        criteriaValue = (Map<String, Object>) ValueTools.toNativeValue(criteria);
                    }
                    NorthwindDb.FindAll findAll = new NorthwindDb.FindAll(entity.value, criteriaValue);
                    NorthwindDbCls.NORTHWIND_DB.send(Envelope.createRequest(findAll,
                        this, new NorthwindDbAdapterId(message, envelope.requester(), envelope.requestId())));
                } else if (message.label().equals(NorthwindDbCls.FIND_BY_KEY_STR)) {
                    Str entity = (Str) message.findValue(NorthwindDbCls.ENTITY_STR);
                    CompleteRec key = (CompleteRec) message.findValue(NorthwindDbCls.KEY_STR);
                    NorthwindDb.FindByKey findByKey = new NorthwindDb.FindByKey(entity.value,
                        (Map<String, Object>) ValueTools.toNativeValue(key));
                    NorthwindDbCls.NORTHWIND_DB.send(Envelope.createRequest(findByKey,
                        this, new NorthwindDbAdapterId(message, envelope.requester(), envelope.requestId())));
                } else {
                    throw new IllegalArgumentException("Invalid request:" + envelope);
                }
            } else if (envelope.isResponse()) {
                NorthwindDbAdapterId id = (NorthwindDbAdapterId) envelope.requestId();
                CompleteRec originalMessage = (CompleteRec) id.originalMessage;
                Str entity = (Str) originalMessage.findValue(NorthwindDbCls.ENTITY_STR);
                String entityName = entity.value;
                RecDesc entityDesc = NorthwindDescs.NORTHWIND_DESCS_BY_ENTITY.get(entityName);
                Complete responseMessage;
                if (originalMessage.label().equals(NorthwindDbCls.FIND_ALL_STR)) {
                    responseMessage = ValueTools.toKernelValue(envelope.message(), new ArrayDesc(entityDesc));
                } else if (originalMessage.label().equals(NorthwindDbCls.FIND_BY_KEY_STR)) {
                    responseMessage = ValueTools.toKernelValue(envelope.message(), entityDesc);
                } else {
                    throw new IllegalArgumentException("Invalid response:" + envelope);
                }
                id.originalRequester.send(Envelope.createResponse(responseMessage, id.originalRequestId));
            } else {
                throw new IllegalArgumentException("Not a request or response:" + envelope);
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class NorthwindDbAdapterId {
        final Object originalMessage;
        final ActorRef originalRequester;
        final Object originalRequestId;

        NorthwindDbAdapterId(Object originalMessage, ActorRef originalRequester, Object originalRequestId) {
            this.originalMessage = originalMessage;
            this.originalRequester = originalRequester;
            this.originalRequestId = originalRequestId;
        }
    }

    private static final class NorthwindDbCfg extends OpaqueValue implements NativeActorCfg {

        NorthwindDbCfg() {
        }

        @Override
        public final ActorRef spawn(Address address, ActorSystem system) {
            return new NorthwindDbAdapter(address);
        }
    }

    public static final class NorthwindDbCls implements CompleteObj {

        static final Str CRITERIA_STR = Str.of("criteria");
        static final Str ENTITY_STR = Str.of("entity");
        static final Str FIND_ALL_STR = Str.of("findAll");
        static final Str FIND_BY_KEY_STR = Str.of("findByKey");
        static final Str KEY_STR = Str.of("key");

        static final Executor NORTHWIND_DB_EXECUTOR = new AffinityExecutor("NorthwindDb", 4);
        static final ActorSystem NORTHWIND_DB_SYSTEM = ActorSystem.builder()
            .setName("NorthwindDb")
            .setExecutor(NORTHWIND_DB_EXECUTOR)
            .build();

        public static final NorthwindDb NORTHWIND_DB = new NorthwindDb(Address.create("northwind_db"),
            NORTHWIND_DB_SYSTEM, 4, 0);

        private static final NorthwindDbCls SINGLETON = new NorthwindDbCls();
        private static final int NORTHWIND_DB_CTOR_ARG_COUNT = 1;
        private static final CompleteProc NORTHWIND_DB_CLS_NEW = NorthwindDbMod::clsNew;

        private NorthwindDbCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.NEW)) {
                return NORTHWIND_DB_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
