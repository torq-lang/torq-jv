/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Int32;
import org.torqlang.util.SourceSpan;

public final class Int32AsType extends AbstractLang implements Int32Type, NumAsType, FeatureAsType {

    public final Int32 int32;

    public Int32AsType(Int32 int32, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.int32 = int32;
    }

    public static Int32AsType create(Int32 int32) {
        return new Int32AsType(int32, SourceSpan.emptySourceSpan());
    }

    @Override
    public final <T, R> R accept(LangVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitScalarAsType(this, state);
    }

    @Override
    public final Int32 value() {
        return int32;
    }
}
