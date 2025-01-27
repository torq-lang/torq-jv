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

public abstract class FuncLang extends AbstractLang implements StmtOrExpr {

    public final List<Pat> formalArgs;
    public final TypeAnno returnType;
    public final SeqLang body;

    public FuncLang(List<Pat> formalArgs, TypeAnno returnType, SeqLang body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.formalArgs = nullSafeCopyOf(formalArgs);
        this.returnType = returnType;
        this.body = body;
    }

}
