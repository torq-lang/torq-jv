/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public enum LexerTokenType {
    CHAR_TOKEN,
    COMMENT_TOKEN,
    DEC_TOKEN,
    EOF_TOKEN,
    FLT_TOKEN,
    IDENT_TOKEN,
    INT_TOKEN,
    KEYWORD_TOKEN,
    ONE_CHAR_TOKEN,
    STR_TOKEN,
    THREE_CHAR_TOKEN,
    TWO_CHAR_TOKEN,
    UNKNOWN_TOKEN,
}
