/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

/*
 * Type inference maps Lang elements to types using system identity, except for IdentAsExpr and IdentAsPat. These
 * elements are identified using their contained identifier. This is a reasonable exception because of the nature
 * of identifiers, which is to create "identifiable" variables that can be shared with other Lang elements. All
 * other Lang elements are identified using the system implementation for equals and hashCode.
 */
public interface Lang extends SourceSpan {

    <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception;

    void setTypeScope(TypeScope typeScope);

    TypeScope typeScope();

}
