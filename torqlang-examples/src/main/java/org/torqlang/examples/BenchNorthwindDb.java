/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.*;

import java.util.Map;
import java.util.concurrent.Executor;

/*
 * Example data:
 *     Example data must be copied from the project directory `resources/northwind/` to the local home
 *     directory `/home/USER/.torq_lang/northwind`.
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindDb
 * Run with 3 hardware threads:
 *     taskset -c 0-2 java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindDb
 */
public final class BenchNorthwindDb {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new BenchNorthwindDb().perform();
        }
        System.exit(0);
    }

    public static void printTimingResults(String owner, long start, long stop, int readCount) {
        long totalTimeMillis = stop - start;
        System.out.println(owner);
        System.out.printf("  Total time: %,d millis\n", totalTimeMillis);
        System.out.printf("  Total reads: %,d\n", readCount);
        System.out.printf("  Millis per read: %,.5f\n", ((double) totalTimeMillis / readCount));
        double readsPerSecond = 1_000.0 / totalTimeMillis;
        System.out.printf("  Reads per second: %,.2f\n", (readsPerSecond * readCount));
    }

    private int checkResponses(Sample... samples) throws Exception {
        for (Sample sample : samples) {
            NorthwindFiles.checkResponse(sample.id, sample.result);
        }
        return samples.length;
    }

    private FutureResponse findByKey(NorthwindDb db, Map<String, Object> key) {
        FutureResponse futureResp = new FutureResponse(Address.create("future_response"));
        Envelope rqs = Envelope.createRequest(new NorthwindDb.FindByKey("customers", key),
            futureResp, "find_request");
        db.send(rqs);
        return futureResp;
    }

    private void perform() throws Exception {
        int concurrencyLevel = Runtime.getRuntime().availableProcessors();
        Executor executor = new AffinityExecutor(Runtime.getRuntime().availableProcessors());
        ActorSystem system = ActorSystem.builder()
            .setName("NorthwindDb")
            .setExecutor(executor)
            .build();
        NorthwindDb db = new NorthwindDb(Address.create("northwind_db"), system, concurrencyLevel, 0);
        int iterCount = 100_000;
        for (int i = 0; i < iterCount; i++) {
            performSampling(db);
        }
        int readCount = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterCount; i++) {
            // Take 31 samples per iteration
            readCount += performSampling(db);
        }
        long stop = System.currentTimeMillis();
        printTimingResults(getClass().getSimpleName(), start, stop, readCount);
    }

    private int performSampling(NorthwindDb db) throws Exception {
        // perform 31 concurrent samples
        return checkResponses(
            new Sample(3, findByKey(db, Map.of("id", 3L))),
            new Sample(7, findByKey(db, Map.of("id", 7L))),
            new Sample(9, findByKey(db, Map.of("id", 9L))),
            new Sample(11, findByKey(db, Map.of("id", 11L))),
            new Sample(17, findByKey(db, Map.of("id", 17L))),
            new Sample(23, findByKey(db, Map.of("id", 23L))),
            new Sample(2, findByKey(db, Map.of("id", 2L))),
            new Sample(6, findByKey(db, Map.of("id", 6L))),
            new Sample(8, findByKey(db, Map.of("id", 8L))),
            new Sample(10, findByKey(db, Map.of("id", 10L))),
            new Sample(16, findByKey(db, Map.of("id", 16L))),
            new Sample(22, findByKey(db, Map.of("id", 22L))),
            new Sample(4, findByKey(db, Map.of("id", 4L))),
            new Sample(8, findByKey(db, Map.of("id", 8L))),
            new Sample(10, findByKey(db, Map.of("id", 10L))),
            new Sample(12, findByKey(db, Map.of("id", 12L))),
            new Sample(18, findByKey(db, Map.of("id", 18L))),
            new Sample(24, findByKey(db, Map.of("id", 24L))),
            new Sample(5, findByKey(db, Map.of("id", 5L))),
            new Sample(9, findByKey(db, Map.of("id", 9L))),
            new Sample(11, findByKey(db, Map.of("id", 11L))),
            new Sample(13, findByKey(db, Map.of("id", 13L))),
            new Sample(19, findByKey(db, Map.of("id", 19L))),
            new Sample(25, findByKey(db, Map.of("id", 25L))),
            new Sample(6, findByKey(db, Map.of("id", 6L))),
            new Sample(10, findByKey(db, Map.of("id", 10L))),
            new Sample(12, findByKey(db, Map.of("id", 12L))),
            new Sample(14, findByKey(db, Map.of("id", 14L))),
            new Sample(20, findByKey(db, Map.of("id", 20L))),
            new Sample(26, findByKey(db, Map.of("id", 26L))),
            new Sample(8, findByKey(db, Map.of("id", 8L)))
        );
    }

    record Sample(int id, FutureResponse result) {
    }

}
