/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.ErrorWithSourceSpan;
import org.torqlang.util.SourceSpan;

public final class ParserError extends ErrorWithSourceSpan {

    private final SourceSpan sourceSpan;

    public ParserError(String message, SourceSpan sourceSpan) {
        super(message);
        this.sourceSpan = sourceSpan;
    }

    @Override
    public final SourceSpan sourceSpan() {
        return sourceSpan;
    }

}
