/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class TypeApply extends AbstractLang implements Type {

    public final IdentAsType name;
    public final List<Type> typeArgs;

    public TypeApply(IdentAsType name,
                     List<? extends Type> typeArgs,
                     SourceSpan sourceSpan)
    {
        super(sourceSpan);
        this.name = name;
        this.typeArgs = nullSafeCopyOf(typeArgs);
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitTypeApply(this, state);
    }

}
