/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.examples.BenchTools.MapSample;
import org.torqlang.local.*;

import java.util.Map;

import static org.torqlang.examples.NorthwindDbMod.NORTHWIND_DB;

/*
 * Example data:
 *     Northwind example data must be copied from the project directory
 *     `src/main/resources/org/torqlang/examples/data/northwind/` to the local home directory
 *     `/home/USER/.torq/resources/org/torqlang/examples/data/northwind/`.
 *
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/.torq/lib -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindDb
 *
 * Run with 3 hardware threads:
 *     taskset -c 0-2 <<nest-previous-java-expression-here>>
 *
 * Run in debug mode by adding the following "-agentlib" option to the previous java expression:
 *     -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
 */
public final class BenchNorthwindDb {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new BenchNorthwindDb().perform();
        }
        System.exit(0);
    }

    private FutureResponse findByKey(NorthwindDb db, Map<String, Object> key) {
        FutureResponse futureResp = new FutureResponse(Address.create("future_response"));
        Envelope rqs = Envelope.createRequest(new NorthwindDb.FindByKey("customers", key),
            futureResp, "find_request");
        db.send(rqs);
        return futureResp;
    }

    private void perform() throws Exception {
        // SETUP
        int iterCount = 100_000;
        // WARMUP
        for (int i = 0; i < iterCount; i++) {
            performSampling(NORTHWIND_DB);
        }
        // SAMPLES
        int readCount = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterCount; i++) {
            // Take 31 samples per iteration
            readCount += performSampling(NORTHWIND_DB);
        }
        long stop = System.currentTimeMillis();
        // REPORTING
        BenchTools.printTimingResults(getClass().getSimpleName(), start, stop, readCount);
    }

    private int performSampling(NorthwindDb db) throws Exception {
        // perform 31 concurrent record samples
        return BenchTools.checkMapResponses(
            new MapSample(3, findByKey(db, Map.of("id", 3L))),
            new MapSample(7, findByKey(db, Map.of("id", 7L))),
            new MapSample(9, findByKey(db, Map.of("id", 9L))),
            new MapSample(11, findByKey(db, Map.of("id", 11L))),
            new MapSample(17, findByKey(db, Map.of("id", 17L))),
            new MapSample(23, findByKey(db, Map.of("id", 23L))),
            new MapSample(2, findByKey(db, Map.of("id", 2L))),
            new MapSample(6, findByKey(db, Map.of("id", 6L))),
            new MapSample(8, findByKey(db, Map.of("id", 8L))),
            new MapSample(10, findByKey(db, Map.of("id", 10L))),
            new MapSample(16, findByKey(db, Map.of("id", 16L))),
            new MapSample(22, findByKey(db, Map.of("id", 22L))),
            new MapSample(4, findByKey(db, Map.of("id", 4L))),
            new MapSample(8, findByKey(db, Map.of("id", 8L))),
            new MapSample(10, findByKey(db, Map.of("id", 10L))),
            new MapSample(12, findByKey(db, Map.of("id", 12L))),
            new MapSample(18, findByKey(db, Map.of("id", 18L))),
            new MapSample(24, findByKey(db, Map.of("id", 24L))),
            new MapSample(5, findByKey(db, Map.of("id", 5L))),
            new MapSample(9, findByKey(db, Map.of("id", 9L))),
            new MapSample(11, findByKey(db, Map.of("id", 11L))),
            new MapSample(13, findByKey(db, Map.of("id", 13L))),
            new MapSample(19, findByKey(db, Map.of("id", 19L))),
            new MapSample(25, findByKey(db, Map.of("id", 25L))),
            new MapSample(6, findByKey(db, Map.of("id", 6L))),
            new MapSample(10, findByKey(db, Map.of("id", 10L))),
            new MapSample(12, findByKey(db, Map.of("id", 12L))),
            new MapSample(14, findByKey(db, Map.of("id", 14L))),
            new MapSample(20, findByKey(db, Map.of("id", 20L))),
            new MapSample(26, findByKey(db, Map.of("id", 26L))),
            new MapSample(8, findByKey(db, Map.of("id", 8L)))
        );
    }

}
