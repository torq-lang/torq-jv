/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;

public interface EvaluatorPerformed {
    DebugInstrListener debugInstrListener();

    Env env();

    Ident exprIdent();

    Kernel kernel();

    Env rootEnv();

    StmtOrExpr stmtOrExpr();

    String source();

    long timeSlice();

    Var varAtName(String name);
}
