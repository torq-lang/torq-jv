/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class ModuleEntry implements Comparable<ModuleEntry> {
    public final String path;
    public final CompleteRec moduleRec;

    public ModuleEntry(String path, CompleteRec moduleRec) {
        this.path = path;
        this.moduleRec = moduleRec;
    }

    @Override
    public final int compareTo(ModuleEntry moduleEntry) {
        return path.compareTo(moduleEntry.path);
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ModuleEntry that = (ModuleEntry) other;
        return Objects.equals(path, that.path);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(path);
    }
}
