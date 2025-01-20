/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.DebugStmtListener;
import org.torqlang.klvm.Env;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Var;

public interface EvaluatorInit {
    EvaluatorInit addVar(Ident ident);

    EvaluatorInit addVar(Ident ident, Var var);

    EvaluatorInit setDebugStmtListener(DebugStmtListener listener);

    EvaluatorInit setExprIdent(Ident exprIdent);

    EvaluatorInit setRootEnv(Env rootEnv);

    EvaluatorReady setSntcOrExpr(SntcOrExpr sntcOrExpr);

    EvaluatorReady setSource(String source);

    EvaluatorInit setTimeSlice(long timeSlice);
}
