/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.klvm.CellMod.CellObj;
import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class SetCellValueInstr extends AbstractInstr {

    public final Ident cell;
    public final CompleteOrIdent value;

    public SetCellValueInstr(Ident cell, CompleteOrIdent value, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.cell = cell;
        this.value = value;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitSetCellValueInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(cell, knownBound, lexicallyFree);
        CompleteOrIdent.captureLexicallyFree(value, knownBound, lexicallyFree);
    }

    public final CompleteOrIdent cell() {
        return cell;
    }

    @Override
    public void compute(Env env, Machine machine) throws WaitException {
        CellObj cellObj = (CellObj) cell.resolveValue(env);
        ValueOrVar valueRes = value.resolveValueOrVar(env);
        cellObj.set(valueRes);
    }

    public final CompleteOrIdent value() {
        return value;
    }

}
