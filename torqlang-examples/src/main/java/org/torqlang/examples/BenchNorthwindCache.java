/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import java.util.Map;

public final class BenchNorthwindCache {

    public static void main(String[] args) throws Exception {
        new BenchNorthwindCache().perform();
    }

    public final void perform() throws Exception {
        int iterations = 100_000;
        String collName = "customers";
        NorthwindCache cache = new NorthwindCache();
        NorthwindColl coll = NorthwindFiles.fetchColl(cache, NorthwindFiles.FILES_DIR, collName);
        cache.data.put(collName, coll);
        // WARMUP
        performRepeatedly(cache, collName, iterations);
        // SAMPLES
        long start = System.currentTimeMillis();
        int readCount = performRepeatedly(cache, collName, iterations);
        long stop = System.currentTimeMillis();
        long totalTimeMillis = stop - start;
        System.out.println(BenchTools.formatTotalsMessage(getClass().getSimpleName(), totalTimeMillis, readCount));
    }

    private int performRepeatedly(NorthwindCache cache, String collName, int iterations)
        throws Exception
    {
        int recordInFile = 29;
        int readCount = 0;
        for (int i = 0; i < iterations; i++) {
            long id = (i % recordInFile) + 1;
            performRead(cache, collName, Map.of("id", id));
            readCount ++;
            id = ((i + 1) % recordInFile) + 1;
            performRead(cache, collName, Map.of("id", id));
            readCount ++;
        }
        return readCount;
    }

    private void performRead(NorthwindCache cache, String collName, Map<String, Object> key)
        throws Exception
    {
        Map<String, Object> rec = NorthwindFiles.fetchRec(cache, NorthwindFiles.FILES_DIR, collName, key);
        if (rec == null) {
            throw new IllegalStateException("Rec not found: " + key);
        }
        Map<String, Object> actualKey = NorthwindFiles.extractKey(rec, key.keySet());
        if (!key.equals(actualKey)) {
            throw new IllegalStateException("key:" + key + " != actualKey:" + actualKey);
        }
    }

}
