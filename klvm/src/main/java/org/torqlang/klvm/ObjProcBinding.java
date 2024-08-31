/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class ObjProcBinding<T extends Obj> implements Proc {

    private final T obj;
    private final ObjProc<T> proc;

    public ObjProcBinding(T obj, ObjProc<T> proc) {
        this.obj = obj;
        this.proc = proc;
    }

    @Override
    public final void apply(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        proc.apply(obj, ys, env, machine);
    }

    @Override
    public final boolean isValidKey() {
        return true;
    }

    public final T obj() {
        return obj;
    }

    public final ObjProc<T> proc() {
        return proc;
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
