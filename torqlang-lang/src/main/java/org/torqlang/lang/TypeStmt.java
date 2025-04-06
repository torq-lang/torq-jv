/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;

import java.util.List;

/*
 * The type statement can define composite data types and data type aliases:
 *     - Composite -- defines label and fields (feature-value pairs) as a type with selection
 *         - Struct -- support unification and entailment
 *             - Array -- no label, Int32 features starting at 0, undetermined size, single value type
 *             - Rec -- label, mixed features, determined size, multiple value types
 *             - Tuple -- label, Int32 features starting at 0, determined size, multiple value types
 *         - Obj -- no unification, requires equals and hash_code, and can have hidden state
 *
 * Examples:
 *
 *     type <<name>><<type-params>> = <<type-expr>>
 *
 *     Array literal
 *         new Array[Int32](0, 1, 2, 3, 4)
 *     <<type-expr>>
 *         Array[Int32]
 *
 *     Tuple literal
 *         [0, 1, 2, 3, 4]
 *     <<type-expr>>
 *         Tuple[Int32, Int32, Int32, Int32]
 *
 *     Tuple literal
 *         [0, 'Bob']
 *     <<type-expr>>
 *         Tuple[Int32, Str]
 *
 *     Record literal
 *         {'name': 'Sue'}
 *     <<type-expr>>
 *         {'name': Str}
 *
 *     Type alias
 *         type MyArray = Array[Int32]
 */
public class TypeStmt extends AbstractLang implements NameDecl, Stmt {

    public final Ident name;
    public final List<TypeParam> typeParams;
    public final Type body;

    public TypeStmt(Ident name, List<TypeParam> typeParams, Type body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.name = name;
        this.typeParams = typeParams;
        this.body = body;
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitTypeStmt(this, state);
    }

    @Override
    public Ident name() {
        return name;
    }
}
