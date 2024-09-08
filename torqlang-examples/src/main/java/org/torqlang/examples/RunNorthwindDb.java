/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.*;

import java.util.concurrent.Executor;

/*
 * Example data:
 *     Example data must be copied from the project directory `resources/northwind/` to the local home
 *     directory `/home/USER/.torq_lang/northwind`.
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.RunNorthwindDb
 * Run with 3 hardware threads:
 *     taskset -c 0-2 java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.RunNorthwindDb
 */
public final class RunNorthwindDb {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new RunNorthwindDb().perform();
        }
        System.exit(0);
    }

    private int checkResponses(Sample... samples) throws Exception {
        for (Sample sample : samples) {
            NorthwindTools.checkResponse(sample.id, sample.result);
        }
        return samples.length;
    }

    private FutureResponse findById(NorthwindDb db, long id) {
        FutureResponse futureResp = new FutureResponse(Address.create("future_response"));
        Envelope rqs = Envelope.createRequest(new NorthwindDb.FindById("customers", id), futureResp, "find_request");
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
        NorthwindTools.printTimingResults(getClass().getSimpleName(), start, stop, readCount);
    }

    private int performSampling(NorthwindDb db) throws Exception {
        // perform 31 concurrent samples
        return checkResponses(
            new Sample(3, findById(db, 3)),
            new Sample(7, findById(db, 7)),
            new Sample(9, findById(db, 9)),
            new Sample(11, findById(db, 11)),
            new Sample(17, findById(db, 17)),
            new Sample(23, findById(db, 23)),
            new Sample(2, findById(db, 2)),
            new Sample(6, findById(db, 6)),
            new Sample(8, findById(db, 8)),
            new Sample(10, findById(db, 10)),
            new Sample(16, findById(db, 16)),
            new Sample(22, findById(db, 22)),
            new Sample(4, findById(db, 4)),
            new Sample(8, findById(db, 8)),
            new Sample(10, findById(db, 10)),
            new Sample(12, findById(db, 12)),
            new Sample(18, findById(db, 18)),
            new Sample(24, findById(db, 24)),
            new Sample(5, findById(db, 5)),
            new Sample(9, findById(db, 9)),
            new Sample(11, findById(db, 11)),
            new Sample(13, findById(db, 13)),
            new Sample(19, findById(db, 19)),
            new Sample(25, findById(db, 25)),
            new Sample(6, findById(db, 6)),
            new Sample(10, findById(db, 10)),
            new Sample(12, findById(db, 12)),
            new Sample(14, findById(db, 14)),
            new Sample(20, findById(db, 20)),
            new Sample(26, findById(db, 26)),
            new Sample(8, findById(db, 8))
        );
    }

    record Sample(int id, FutureResponse result) {
    }

}
