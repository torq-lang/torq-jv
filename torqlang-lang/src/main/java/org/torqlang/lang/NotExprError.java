/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;


public final class NotExprError extends LangError {

    public static final String NOT_AN_EXPRESSION = "Not an expression";

    public NotExprError(Lang lang) {
        super(NOT_AN_EXPRESSION, lang);
    }

}
