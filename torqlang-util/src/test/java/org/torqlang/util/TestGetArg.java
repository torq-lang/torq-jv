/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestGetArg {

    @Test
    public void test() {
        List<String> mainArgs = List.of();
        List<String> optionArgs = GetArg.get("--notfound", mainArgs);
        assertNull(optionArgs);

        mainArgs = List.of("--no-args");
        optionArgs = GetArg.get("--no-args", mainArgs);
        assertNotNull(optionArgs);
        assertTrue(optionArgs.isEmpty());

        mainArgs = List.of("-option1", "a");
        optionArgs = GetArg.get("-option1", mainArgs);
        assertNotNull(optionArgs);
        assertEquals(List.of("a"), optionArgs);

        // Second duplicate option is an error
        final List<String> mainArgs2 = List.of("-option1", "a", "-option1", "b");
        IllegalArgumentException exc = assertThrows(IllegalArgumentException.class,
            () -> GetArg.get("-option1", mainArgs2));
        assertEquals("Duplicate option error: -option1", exc.getMessage());

        mainArgs = List.of("-option1", "a", "-option2", "b");
        optionArgs = GetArg.get("-option2", mainArgs);
        assertNotNull(optionArgs);
        assertEquals(List.of("b"), optionArgs);

        mainArgs = List.of("-option1", "a", "-option2", "b", "c");
        optionArgs = GetArg.get("-option2", mainArgs);
        assertNotNull(optionArgs);
        assertEquals(List.of("b", "c"), optionArgs);
    }

}
