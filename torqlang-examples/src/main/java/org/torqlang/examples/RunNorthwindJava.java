/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RunNorthwindJava {

    public static void main(String[] args) throws Exception {
        new RunNorthwindJava().perform();
    }

    public void perform() throws Exception {
        int iterations = 100_000;
        String collName = "customers";
        Map<String, List<Map<String, Object>>> cache = new HashMap<>();
        List<Map<String, Object>> coll = NorthwindTools.fetchColl(cache, NorthwindTools.ROOT_DIR, collName);
        cache.put(collName, coll);
        performRepeatedly(cache, collName, iterations);
        long start = System.currentTimeMillis();
        int readCount = performRepeatedly(cache, collName, iterations);
        long stop = System.currentTimeMillis();
        long totalTimeMillis = stop - start;
        System.out.println(getClass().getSimpleName());
        System.out.printf("  Total time: %,d millis\n", totalTimeMillis);
        System.out.printf("  Total reads: %,d\n", readCount);
        System.out.printf("  Millis per read: %,.5f\n", ((double) totalTimeMillis / readCount));
        double readsPerSecond = 1_000.0 / totalTimeMillis;
        System.out.printf("  Reads per second: %,.2f\n", (readsPerSecond * readCount));
    }

    private int performRepeatedly(Map<String, List<Map<String, Object>>> cache, String collName, int iterations)
        throws Exception
    {
        int recordInFile = 29;
        int readCount = 0;
        for (int i = 0; i < iterations; i++) {
            int id = (i % recordInFile) + 1;
            performRead(cache, collName, id);
            readCount ++;
            id = ((i + 1) % recordInFile) + 1;
            performRead(cache, collName, id);
            readCount ++;
        }
        return readCount;
    }

    private void performRead(Map<String, List<Map<String, Object>>> cache, String collName, int id)
        throws Exception
    {
        Map<String, Object> rec = NorthwindTools.fetchRec(cache, NorthwindTools.ROOT_DIR, collName, id);
        if (rec == null) {
            throw new IllegalStateException("Rec not found: " + id);
        }
        long actualId = (Long) rec.get("id");
        if (actualId != id) {
            throw new IllegalStateException("id:" + id + " != actualId:" + actualId);
        }
    }

}
