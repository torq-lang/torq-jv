/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestResult {

    @Test
    public void test() {
        Result<String, Failure> r = new Result.Error<>(new Failure("test failure"));
        assertTrue(r.isError());
        assertFalse(r.isValue());
        assertEquals("test failure", r.error().message);

        r = new Result.Value<>("test success");
        assertFalse(r.isError());
        assertTrue(r.isValue());
        assertEquals("test success", r.value());
    }

    record Failure(String message) {};

}
