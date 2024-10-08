/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.ErrorWithSourceSpan;
import org.torqlang.util.SourceSpan;

public abstract class GeneratorError extends ErrorWithSourceSpan {

    private final Lang lang;

    public GeneratorError(String message, Lang lang) {
        super(message);
        this.lang = lang;
    }

    public final Lang lang() {
        return lang;
    }

    public final SourceSpan sourceSpan() {
        return lang;
    }

}
