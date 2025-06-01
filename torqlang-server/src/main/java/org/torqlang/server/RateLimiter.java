/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

public final class RateLimiter {

    private static final long ONE_SECOND = 1000;

    private final Object lock = new Object();
    private final int limit;
    private volatile long startTime;
    private volatile int remaining;

    private RateLimiter(int limit) {
        this.limit = limit;
        this.startTime = System.currentTimeMillis();
        this.remaining = limit;
    }

    public static RateLimiter create(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0");
        }
        return new RateLimiter(limit);
    }

    public final boolean rateExceeded() {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            long elapsedTime = now - startTime;
            if (elapsedTime > ONE_SECOND) {
                startTime = now;
                remaining = limit;
                return false;
            } else {
                if (remaining > 0) {
                    remaining--;
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

}
