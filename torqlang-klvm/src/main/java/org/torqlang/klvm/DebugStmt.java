/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

/*
 * A debug statement behaves like a cursor. As a machine executes, the debug statement gives the debug listener a
 * chance to act on the next statement to run. The debug listener can suspend and resume running by setting a wait
 * barrier. After the debug listener is given its chance to act, the debug statement performs the next statement and
 * pushes itself back onto the stack before returning from compute.
 *
 * How to debug:
 * - To "step" through a program, the debug listener alternates between suspending and resuming.
 * - To "run to" a program line, the debug listener suspends when the statement for that line is reached.
 */
public final class DebugStmt extends AbstractStmt {

    private final DebugStmtListener listener;

    private Stmt nextStmt;
    private Env nextEnv;
    private Var barrier;

    public DebugStmt(DebugStmtListener listener, Stmt nextStmt, Env nextEnv, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.nextStmt = nextStmt;
        this.nextEnv = nextEnv;
        this.listener = listener;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitDebugStmt(this, state);
    }

    public final Var barrier() {
        return barrier;
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        nextStmt.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        if (listener != null) {
            listener.onDebugStmt(this, machine);
        }
        if (barrier != null) {
            throw new WaitVarException(barrier);
        }
        nextStmt.compute(nextEnv, machine);
        Stack next = machine.popStackEntry();
        if (next != null) {
            nextStmt = next.stmt;
            nextEnv = next.env;
            machine.pushStackEntry(this, Env.emptyEnv());
        }
    }

    public final Env nextEnv() {
        return nextEnv;
    }

    public final Stmt nextStmt() {
        return nextStmt;
    }

    public final void resume() {
        this.barrier = null;
    }

    public final void suspend() {
        this.barrier = new Var();
    }

}
