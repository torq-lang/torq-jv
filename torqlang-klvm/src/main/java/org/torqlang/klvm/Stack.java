/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public class Stack implements Kernel {

    public final Instr instr;
    public final Env env;
    public final Stack next;
    public final int size;

    public Stack(Instr instr, Env env, Stack next) {
        this.instr = instr;
        this.env = env;
        this.next = next;
        this.size = next == null ? 1 : next.size + 1;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitStack(this, state);
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
