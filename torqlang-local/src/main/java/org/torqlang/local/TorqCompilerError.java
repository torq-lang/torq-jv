/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.util.Message;

import java.util.List;

public class TorqCompilerError extends RuntimeException {
    private final List<Message> details;

    TorqCompilerError(String message, List<Message> details) {
        super(message);
        this.details = details;
    }

    public final List<Message> details() {
        return details;
    }
}
