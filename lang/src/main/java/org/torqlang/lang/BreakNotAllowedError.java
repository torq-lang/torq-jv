/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;


public final class BreakNotAllowedError extends GeneratorError {

    public static final String BREAK_NOT_ALLOWED = "Break not allowed";

    public BreakNotAllowedError(Lang lang) {
        super(BREAK_NOT_ALLOWED, lang);
    }

}
