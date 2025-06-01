/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

/*
 * A type statement defines a type constructor. The `name` property is the name used in your code to create instances
 * of the type, and the body defines the type instance created when the type constructor is applied. The `typeParams`
 * property defines 0 or more parameters that will be accepted within square brackets when the type constructor is
 * applied.
 */
public class TypeStmt extends AbstractLang implements Stmt {

    public final TypeDecl typeDecl;
    public final Type body;

    public TypeStmt(TypeDecl typeDecl, Type body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.typeDecl = typeDecl;
        this.body = body;
    }

    @Override
    public <T, R> R accept(LangVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitTypeStmt(this, state);
    }

    public IdentAsType name() {
        return typeDecl.name;
    }

    public List<TypeParam> typeParams() {
        return typeDecl.typeParams;
    }

}
