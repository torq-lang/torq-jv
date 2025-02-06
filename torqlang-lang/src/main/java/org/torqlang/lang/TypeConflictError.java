/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public final class TypeConflictError extends LangError {

    public TypeConflictError(Lang lang, MonoType expected, MonoType provided) {
        super("Expected " + expected + " but found " + provided, lang);
    }

}
