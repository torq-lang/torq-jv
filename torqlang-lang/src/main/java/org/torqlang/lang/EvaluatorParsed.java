/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Env;
import org.torqlang.klvm.Ident;

public interface EvaluatorParsed {
    Ident exprIdent();

    EvaluatorGenerated generate() throws Exception;

    long maxTime();

    EvaluatorParsed parse() throws Exception;

    EvaluatorPerformed perform() throws Exception;

    Env rootEnv();

    SntcOrExpr sntcOrExpr();

    String source();
}
