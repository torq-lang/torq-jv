/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.CompleteTuple;
import org.torqlang.klvm.FailedValue;
import org.torqlang.local.Envelope;
import org.torqlang.local.FutureResponse;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public class BenchTools {

    /*
     * Argument order (expected, actual) is the same order used in JUnit assert methods.
     */
    public static void checkExpected(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            String error = "Not expected: " + actual;
            if (actual instanceof FailedValue failedValue) {
                error += "\n" + failedValue.toDetailsString();
            }
            throw new IllegalStateException(error);
        }
    }

    public static void checkFalse(boolean value) {
        if (value) {
            throw new IllegalArgumentException("Not false");
        }
    }

    @SuppressWarnings("unchecked")
    public static void checkMapResponse(int expectedId, FutureResponse futureResponse) throws Exception {
        Envelope resp = futureResponse.future().get(1, TimeUnit.SECONDS);
        if (resp.message() instanceof FailedValue) {
            throw new IllegalStateException("Request failed: " + resp.message());
        }
        Map<String, Object> rec = (Map<String, Object>) resp.message();
        long id = (Long) rec.get("id");
        if (id != expectedId) {
            throw new IllegalStateException("Response id is not " + expectedId);
        }
    }

    public static int checkMapResponses(MapSample... samples) throws Exception {
        for (MapSample sample : samples) {
            checkMapResponse(sample.id, sample.result);
        }
        return samples.length;
    }

    public static void checkNotNull(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
    }

    public static void checkTrue(boolean value) {
        if (!value) {
            throw new IllegalArgumentException("Not true");
        }
    }

    public static void checkTupleResponse(int expectedSize, FutureResponse futureResponse) throws Exception {
        Envelope resp = futureResponse.future().get(1, TimeUnit.SECONDS);
        if (resp.message() instanceof FailedValue) {
            throw new IllegalStateException("Request failed");
        }
        CompleteTuple tuple = (CompleteTuple) resp.message();
        if (tuple.fieldCount() != expectedSize) {
            throw new IllegalStateException("Response size is not " + expectedSize);
        }
    }

    public static int checkTupleResponses(TupleSample... samples) throws Exception {
        for (TupleSample sample : samples) {
            checkTupleResponse(sample.expectedSize, sample.result);
        }
        return samples.length;
    }

    public static String formatTotalsMessage(String header, long totalTimeMillis, int readCount) {
        double readsPerSecond = 1_000.0 / totalTimeMillis;
        return header + System.lineSeparator() +
            "  " + String.format("Total time: %,d millis", totalTimeMillis) + System.lineSeparator() +
            "  " + String.format("Total reads: %,d", readCount) + System.lineSeparator() +
            "  " + String.format("Millis per read: %,.5f", ((double) totalTimeMillis / readCount)) + System.lineSeparator() +
            "  " + String.format("Reads per second: %,.2f", (readsPerSecond * readCount));
    }

    public static void performWithErrorCheck(Performer performer) throws Exception {
        try {
            performer.perform();
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 5, 5);
        }
    }

    public static void printTimingResults(String owner, long start, long stop, int readCount) {
        long totalTimeMillis = stop - start;
        System.out.println(formatTotalsMessage(owner, totalTimeMillis, readCount));
    }

    public interface Performer {
        void perform() throws Exception;
    }

    public record MapSample(int id, FutureResponse result) {
    }

    public record TupleSample(int expectedSize, FutureResponse result) {
    }
}
