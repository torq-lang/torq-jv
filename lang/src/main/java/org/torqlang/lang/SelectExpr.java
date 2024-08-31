/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

public abstract class SelectExpr extends AbstractLang implements BuiltInApplyExpr {

    public final SntcOrExpr recExpr;
    public final SntcOrExpr featureExpr;

    public SelectExpr(SntcOrExpr recExpr, SntcOrExpr featureExpr, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.recExpr = recExpr;
        this.featureExpr = featureExpr;
    }

}
