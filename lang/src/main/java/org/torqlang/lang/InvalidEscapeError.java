/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;


public final class InvalidEscapeError extends GeneratorError {

    public static final String INVALID_ESCAPE_ERROR = "Invalid escape error";

    public InvalidEscapeError(Lang lang) {
        super(INVALID_ESCAPE_ERROR, lang);
    }

}
