/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.HashSet;
import java.util.Set;

public final class IfElseInstr extends AbstractInstr {

    public final CompleteOrIdent x;
    public final Instr consequent;
    public final Instr alternate;

    public IfElseInstr(CompleteOrIdent x, Instr consequent, Instr alternate, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.x = x;
        this.consequent = consequent;
        this.alternate = alternate;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitIfElseInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(x, knownBound, lexicallyFree);
        // Copy knownBound to hide out-of-scope identifiers from the alternate instruction
        consequent.captureLexicallyFree(new HashSet<>(knownBound), lexicallyFree);
        // No need to copy knownBound since there are no more peer instructions
        alternate.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {
        // Conditional (the if instruction) [CTM p. 66]
        // If the activation condition is true, E(<x>) is determined, then
        // -- if E(<x>) is not a boolean (true or false) then throw an error
        // -- if E(<x>) is true, then push (<s>1, E) on the stack
        // -- if E(<x>) is true, then push (<s>2, E) on the stack
        Value xRes = x.resolveValue(env);
        if (!(xRes instanceof Bool bool)) {
            throw new NotBoolError(xRes, this);
        }
        if (bool.value) {
            machine.pushStackEntry(consequent, env);
        } else {
            machine.pushStackEntry(alternate, env);
        }
    }

}
