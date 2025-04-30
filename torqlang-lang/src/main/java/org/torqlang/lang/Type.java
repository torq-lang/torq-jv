/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.util.SourceSpan;

/*
 * There are two categories of type names:
 *   1. Built-in - these are the base types provided by Torq, such as `Str` and `Bool`.
 *   2. User-defined - these are defined using the `type` statement.
 *
 * The method `fromIdent` will return an instance of the built-in type or an instance
 * of `IdentAsType` if it is defined by the user.
 *
 * Because we are structurally typed, the type statement actually defines an alias for a
 * type expression.
 *
 * // A type alias with no parameters
 * type MyRec = {
 *     'field': Int32
 * }
 *
 * // A type alias for a record expression that accepts 1 parameter
 * type MyRec[T] = {
 *     'field': T
 * }
 *
 * // A type alias for another type alias that accepts 1 parameter
 * type OtherRec[T] = MyRec[T]
 *
 * // A type alias with no parameters that instantiates another type alias that accepts 1 parameter
 * type OtherRec = MyRec[Int32]
 */
public interface Type extends Lang {

    static Type fromIdent(Ident ident, SourceSpan sourceSpan) {
        return switch (ident.name) {
            case StrType.NAME -> StrType.create(sourceSpan);
            case BoolType.NAME -> BoolType.create(sourceSpan);
            case Int32Type.NAME -> Int32Type.create(sourceSpan);
            case Int64Type.NAME -> Int64Type.create(sourceSpan);
            case Flt32Type.NAME -> Flt32Type.create(sourceSpan);
            case Flt64Type.NAME -> Flt64Type.create(sourceSpan);
            case ArrayType.NAME -> ArrayType.create(sourceSpan);
            case RecType.NAME -> RecType.create(sourceSpan);
            case TupleType.NAME -> TupleType.create(sourceSpan);
            case AnyType.NAME -> AnyType.create(sourceSpan);
            case NullType.NAME -> NullType.create(sourceSpan);
            case EofType.NAME -> EofType.create(sourceSpan);
            case TokenType.NAME -> TokenType.create(sourceSpan);
            case CharType.NAME -> CharType.create(sourceSpan);
            default -> IdentAsType.create(ident, sourceSpan);
        };
    }

}
