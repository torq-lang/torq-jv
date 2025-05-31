/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

/*
 * Criteria Grammar
 * ================
 *
 * The criterial grammar is inspired by the MongoDB grammar for queries. Productions are displayed as all capital
 * letters and hyphenated if they contain multiple words. Strings as literal values are displayed in double quotes,
 * and grammar symbols are displayed in single quotes.
 *
 * The VALUE production defines the JSON compatible values: OBJECT, STRING, NUMBER, BOOLEAN, and NULL.
 *
 * FEATURE:
 *     STRING
 *
 * VALUE:
 *     OBJECT
 *     STRING
 *     NUMBER
 *     BOOLEAN
 *     NULL
 *
 * ARRAY:
 *     '[' VALUE (',' VALUE)* ']'
 *
 * OPER:
 *     REL-OPER
 *     IN-OPER
 *
 * REL-OPER:
 *     "$eq"
 *     "$gt"
 *     "$gte"
 *     "$lt"
 *     "$lte"
 *     "$ne"
 *
 * IN-OPER:
 *     "$in"
 *     "$nin"
 *
 * FIELD-OPER:
 *     FEATURE: VALUE
 *     FEATURE: '{' REL-OPER: VALUE '}
 *     FEATURE: '{' IN-OPER: ARRAY '}
 *
 * EXPR:
 *     '{' FIELD-OPER (',' FIELD-OPER)* '}'
 *     '{' "$not": EXPR '}'
 *     '{' "$and": [EXPR (',' EXPR)*] '}'
 *     '{' "$or": [EXPR (',' EXPR)*] '}'
 */
public class Criteria {
}
