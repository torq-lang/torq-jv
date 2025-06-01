/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestJsonFormatter {

    @Test
    public void test() {

        String s;

        s = Json.format(null);
        assertEquals("null", s);

        s = Json.format(JsonNull.SINGLETON);
        assertEquals("null", s);

        s = Json.format(0);
        assertEquals("0", s);

        s = Json.format(0.005);
        assertEquals("0.005", s);

        s = Json.format(LocalDate.now());
        assertEquals("\"" + LocalDate.now() + "\"", s);

        s = Json.format(new BigDecimal("1.11"));
        assertEquals("\"1.11\"", s);

        s = Json.format(false);
        assertEquals("false", s);

        s = Json.format(true);
        assertEquals("true", s);

        s = Json.format("my-string");
        assertEquals("\"my-string\"", s);

        s = Json.format(List.of());
        assertEquals("[]", s);

        s = Json.format(List.of(1));
        assertEquals("[1]", s);

        s = Json.format(List.of(1, 2));
        assertEquals("[1,2]", s);

        s = Json.format(Map.of());
        assertEquals("{}", s);

        s = Json.format(Map.of("one", 1));
        assertEquals("{\"one\":1}", s);

        s = Json.format(Map.of("one", 1, "two", 2));
        assertTrue("{\"one\":1,\"two\":2}".equals(s) || "{\"two\":2,\"one\":1}".equals(s));
    }

    @Test
    public void testErrors() {
        IllegalArgumentException exc = assertThrows(IllegalArgumentException.class,
            () -> Json.format(new Object()));
        assertTrue(exc.getMessage().startsWith("Invalid JSON request: java.lang.Object"));
    }

}
