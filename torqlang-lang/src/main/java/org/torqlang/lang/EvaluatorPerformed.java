/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Env;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Kernel;
import org.torqlang.klvm.Var;

public interface EvaluatorPerformed {
    Env env();

    Ident exprIdent();

    Kernel kernel();

    long maxTime();

    Env rootEnv();

    SntcOrExpr sntcOrExpr();

    String source();

    Var varAtName(String name);
}
