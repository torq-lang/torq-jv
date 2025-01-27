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

public interface EvaluatorReady {
    DebugInstrListener debugInstrListener();

    Ident exprIdent();

    EvaluatorGenerated generate() throws Exception;

    EvaluatorParsed parse() throws Exception;

    EvaluatorPerformed perform() throws Exception;

    Env rootEnv();

    String source();

    long timeSlice();
}
