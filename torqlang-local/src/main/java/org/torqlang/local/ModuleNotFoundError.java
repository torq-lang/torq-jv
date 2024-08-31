/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.MachineError;

public class ModuleNotFoundError extends MachineError {
    public static final String MODULE_NOT_FOUND = "Module not found";
    public final String path;

    public ModuleNotFoundError(String path) {
        super(MODULE_NOT_FOUND);
        this.path = path;
    }
}
