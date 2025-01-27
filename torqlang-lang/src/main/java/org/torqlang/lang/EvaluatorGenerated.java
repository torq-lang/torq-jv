/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.DebugInstrListener;
import org.torqlang.klvm.Env;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Kernel;

public interface EvaluatorGenerated {
    DebugInstrListener debugInstrListener();

    Ident exprIdent();

    Kernel kernel();

    EvaluatorPerformed perform() throws Exception;

    Env rootEnv();

    StmtOrExpr stmtOrExpr();

    String source();

    long timeSlice();
}
