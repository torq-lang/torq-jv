/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.Stack;
import org.torqlang.util.ErrorWithSourceSpan;
import org.torqlang.util.SourceSpan;

public class SelfRefAskError extends ErrorWithSourceSpan {

    private final SourceSpan sourceSpan;

    public SelfRefAskError(Stack current) {
        this.sourceSpan = current.stmt;
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

}
