/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestResourcesFileBroker {

    @Test
    public void test01() {
        Class<?> reference = MethodHandles.lookup().lookupClass();
        List<FileName> root1 = List.of(new FileName(FileType.DIRECTORY, "a"), new FileName(FileType.DIRECTORY, "b"));
        List<FileName> root2 = List.of(new FileName(FileType.DIRECTORY, "a"), new FileName(FileType.DIRECTORY, "b"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            new ResourcesFileBroker(reference, List.of(root1, root2), List.of());
        });
        assertEquals("Duplicate path", e.getMessage());
    }

}
