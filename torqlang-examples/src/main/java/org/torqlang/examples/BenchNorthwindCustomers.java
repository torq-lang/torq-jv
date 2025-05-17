/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.examples.BenchTools.TupleSample;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.CompleteTuple;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.local.*;
import org.torqlang.util.*;

import static org.torqlang.examples.BenchTools.printTimingResults;
import static org.torqlang.examples.ExamplesSourceBroker.EXAMPLES_ROOT;
import static org.torqlang.examples.ExamplesSourceBroker.NORTHWIND;

/*
 * java -XX:+UseZGC -p ~/.torq/lib -m org.torqlang.examples/org.torqlang.examples.BenchNorthwindCustomers
 */
public final class BenchNorthwindCustomers {

    private static final int TOTAL_CUSTOMERS = 29;

    private static final CompleteTuple CUSTOMERS_PATH = Rec.completeTupleBuilder()
        .addValue(Str.of("customers"))
        .build();
    private static final CompleteRec GET_CUSTOMERS = Rec.completeRecBuilder()
        .setLabel(Str.of("GET"))
        .addField(Str.of("headers"), Rec.completeRecBuilder().build())
        .addField(Str.of("path"), CUSTOMERS_PATH)
        .addField(Str.of("query"), Rec.completeRecBuilder().build())
        .addField(Str.of("context"), Rec.completeRecBuilder().build())
        .build();

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new BenchNorthwindCustomers().perform();
        }
        System.exit(0);
    }

    private void perform() throws Exception {
        // SETUP
        CompleteRec examplesPack = Rec.completeRecBuilder()
            .addField(Str.of("NorthwindDb"), NorthwindDbMod.northwindDbCls())
            .build();
        ActorSystem system = ActorSystem.builder()
            .addDefaultPackages()
            .addPackage("examples", examplesPack)
            .build();
        SourceFileBroker broker = ExamplesSourceBroker.createResourcesBrokerForActors();
        SourceFile customersHandlerSource = broker.source(
            SourceFileBroker.append(SourceFileBroker.append(EXAMPLES_ROOT, NORTHWIND), new FileName(FileType.SOURCE, "CustomersHandler.torq"))
        );
        ActorImage actorImage = Actor.builder()
            .setSystem(system)
            .actorImage(customersHandlerSource.content());
        int iterCount = 10_000;
        // WARMUP
        for (int i = 0; i < iterCount; i++) {
            performSampling(actorImage, TOTAL_CUSTOMERS);
        }
        // SAMPLES
        int readCount = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterCount; i++) {
            readCount += performSampling(actorImage, TOTAL_CUSTOMERS);
        }
        long stop = System.currentTimeMillis();
        // REPORTING
        printTimingResults(getClass().getSimpleName(), start, stop, readCount);
    }

    private TupleSample spawnSample(ActorImage actorImage, int expectedSize) {
        ActorRef actorRef = Actor.spawn(Address.create("test_actor"), actorImage);
        FutureResponse futureResp = new FutureResponse(Address.create("future_response"));
        Envelope rqs = Envelope.createRequest(GET_CUSTOMERS, futureResp, "get_request");
        actorRef.send(rqs);
        return new TupleSample(expectedSize, futureResp);
    }

    private int performSampling(ActorImage actorImage, int expectedSize) throws Exception {
        return BenchTools.checkTupleResponses(
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize),
            spawnSample(actorImage, expectedSize)
        );
    }

}
