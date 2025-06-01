/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

public interface Instr extends DeclOrInstr, SourceSpan {

    void compute(Env env, Machine machine) throws WaitException;

    void pushStackEntries(Machine machine, Env env);

}
