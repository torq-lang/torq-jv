/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;

import java.util.List;

public final class UnionType extends AbstractLang implements Type {

    public final List<Type> types;

    public UnionType(List<Type> types, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.types = types;
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        throw new NeedsImpl();
    }

}
