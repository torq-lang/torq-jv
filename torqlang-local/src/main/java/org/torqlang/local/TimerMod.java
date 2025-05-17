/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.torqlang.local.Envelope.createResponse;

/*
 * A Timer generates a stream of ticks where each tick is preceded by a period of delay.
 *
 * Rules:
 * - Timers are a single-producer, single-consumer design.
 * - A timer can be reused after it reaches end-of-file.
 */
final class TimerMod implements KernelModule {

    public static final Str TIMER_STR = Str.of("Timer");
    public static final Ident TIMER_IDENT = Ident.create(TIMER_STR.value);

    private static final int TIMER_CTOR_ARG_COUNT = 3;

    private final CompleteRec exports;

    private TimerMod() {
        exports = Rec.completeRecBuilder()
            .addField(TIMER_STR, TimerCls.SINGLETON)
            .build();
    }

    public static TimerCls timerCls() {
        return TimerCls.SINGLETON;
    }

    public static TimerMod singleton() {
        return LazySingleton.SINGLETON;
    }

    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        if (ys.size() != TIMER_CTOR_ARG_COUNT) {
            throw new InvalidArgCountError(TIMER_CTOR_ARG_COUNT, ys, "timerCtor");
        }
        Num period = (Num) ys.get(0).resolveValue(env);
        Str timeUnit = (Str) ys.get(1).resolveValue(env);
        TimerCfg config = new TimerCfg(period, timeUnit);
        ys.get(2).resolveValueOrVar(env).bindToValue(config, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    private static final class LazySingleton {
        private static final TimerMod SINGLETON = new TimerMod();
    }

    private static final class Timer extends AbstractActor {

        public static final Str TICKS_FEAT = Str.of("ticks");

        private static final CompleteRec EOF_RECORD = Rec.completeRecBuilder()
            .setLabel(Eof.SINGLETON)
            .addField(Str.of("more"), Bool.FALSE)
            .build();
        private static final ScheduledThreadPoolExecutor SCHEDULED_EXECUTOR =
            new ScheduledThreadPoolExecutor(2, Timer::newTimerThread);
        private static final Object TIMER_CALLBACK = new Object();

        private final Num periodNum;
        private final TimeUnit timeUnit;
        private Envelope activeRequest;
        private int currentTicks;
        private int requestedTicks;
        private ScheduledFuture<?> scheduledFuture;

        public Timer(Address address, ActorSystem system, Num periodNum, Str timeUnitStr) {
            super(address, system.createMailbox(), system.executor(), system.createLogger());
            this.periodNum = periodNum;
            if (timeUnitStr.value.equalsIgnoreCase("microseconds")) {
                timeUnit = TimeUnit.MICROSECONDS;
            } else if (timeUnitStr.value.equalsIgnoreCase("milliseconds")) {
                timeUnit = TimeUnit.MILLISECONDS;
            } else if (timeUnitStr.value.equalsIgnoreCase("seconds")) {
                timeUnit = TimeUnit.SECONDS;
            } else {
                throw new IllegalArgumentException("Not 'microseconds', 'milliseconds', or 'seconds'");
            }
        }

        private static Thread newTimerThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }

        private static CompleteRec validateMessage(Envelope activeRequest) {
            if (!(activeRequest.message() instanceof CompleteRec completeRec)) {
                throw new IllegalArgumentException("Invalid timer request: " + activeRequest);
            }
            if (!completeRec.label().equals(Str.of("request"))) {
                throw new IllegalArgumentException("Invalid timer request: " + activeRequest);
            }
            if (completeRec.fieldCount() != 1) {
                throw new IllegalArgumentException("Timer request must contain a 'ticks' feature");
            }
            return completeRec;
        }

        private static int validateTicks(CompleteRec activeMessage) {
            Complete ticks = activeMessage.findValue(TICKS_FEAT);
            if (!(ticks instanceof Int64 int32)) {
                throw new IllegalArgumentException("Not an Int32");
            }
            return int32.intValue();
        }

        @Override
        protected OnMessageResult onMessage(Envelope[] next) {
            Envelope envelope = next[0];
            try {
                if (envelope.isRequest()) {
                    return onTimerRequest(envelope);
                } else if (envelope.isResponse()) {
                    return onTimerCallback(envelope);
                } else {
                    throw new IllegalArgumentException("Unrecognized message: " + envelope);
                }
            } catch (Throwable throwable) {
                FailedValue failedValue = FailedValue.create(address().toString(), throwable);
                if (envelope.requester() != null) {
                    envelope.requester().send(createResponse(failedValue, envelope.requestId()));
                } else {
                    logError("Timer error:\n" + failedValue.toDetailsString());
                }
            }
            return OnMessageResult.NOT_FINISHED;
        }

        private OnMessageResult onTimerCallback(Envelope envelope) {
            if (!(envelope.message() == TIMER_CALLBACK)) {
                throw new IllegalArgumentException("Invalid timer callback: " + envelope);
            }
            if (activeRequest == null) {
                return OnMessageResult.NOT_FINISHED;
            }
            long now = System.currentTimeMillis();
            if (currentTicks >= requestedTicks) {
                scheduledFuture.cancel(true);
                activeRequest.requester().send(createResponse(EOF_RECORD, activeRequest.requestId()));
                activeRequest = null;
            } else {
                currentTicks++;
                activeRequest.requester().send(createResponse(CompleteTuple.singleton(Int64.of(now)),
                    activeRequest.requestId()));
            }
            return OnMessageResult.NOT_FINISHED;
        }

        private OnMessageResult onTimerRequest(Envelope envelope) {
            if (activeRequest != null) {
                throw new IllegalStateException("Timer is already active: " + envelope);
            }
            activeRequest = envelope;
            requestedTicks = validateTicks(validateMessage(activeRequest));
            scheduledFuture = SCHEDULED_EXECUTOR.scheduleAtFixedRate(() ->
                    this.send(createResponse(TIMER_CALLBACK, activeRequest.requestId())),
                periodNum.longValue(), periodNum.longValue(), timeUnit);
            return OnMessageResult.NOT_FINISHED;
        }
    }

    private static final class TimerCfg extends OpaqueValue implements NativeActorCfg {
        final Num periodNum;
        final Str timeUnitStr;

        TimerCfg(Num periodNum, Str timeUnitStr) {
            this.periodNum = periodNum;
            this.timeUnitStr = timeUnitStr;
        }

        @Override
        public final ActorRef spawn(Address address, ActorSystem system) {
            return new Timer(address, system, periodNum, timeUnitStr);
        }
    }

    static final class TimerCls implements CompleteObj {
        private static final TimerCls SINGLETON = new TimerCls();
        private static final CompleteProc TIMER_CLS_NEW = TimerMod::clsNew;

        private TimerCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.NEW)) {
                return TIMER_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
