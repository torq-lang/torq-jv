/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.Set;

public final class CaseInstr extends AbstractInstr {

    public final CompleteOrIdent x;
    public final ValueOrPtn valueOrPtn;
    public final Instr consequent;

    public CaseInstr(CompleteOrIdent x, ValueOrPtn valueOrPtn, Instr consequent, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.x = x;
        this.valueOrPtn = valueOrPtn;
        this.consequent = consequent;
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitCaseInstr(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        CompleteOrIdent.captureLexicallyFree(x, knownBound, lexicallyFree);
        if (valueOrPtn instanceof Ptn ptn) {
            // Lexically free identifiers will be "escaped" identifiers, such as ~A that are not in the knownBound set
            // All other identifiers will be added to the knownBound set and made visible to the consequent instruction
            ptn.captureLexicallyFree(knownBound, lexicallyFree);
        }
        consequent.captureLexicallyFree(knownBound, lexicallyFree);
    }

    @Override
    public final void compute(Env env, Machine machine) throws WaitException {

        // Pattern matching (case instructions) [CTM p. 67]
        // Given (case <x> of <lit>(<feat>1: <x>1 ... <feat>n: <x>n) then <s>1 else <s>2 end, E)
        // If the activation condition is true, E(<x>) is determined, then
        // -- if the label of E(<x>) is <lit> and its arity is {<feat>1, ..., <feat>n} then
        //    push (<s>1, E + {<x>1 -> E(<x>).<feat>1,...,<x>n -> E(<x>).<feat>n}) on the stack
        // -- otherwise, push {<s>2, E} on the stack

        Value xRes = x.resolveValue(env);
        // Resolve pattern to an IdentPtn (not escaped), ResolvedRecPtn, Value, or null.
        // A null is a failed match that causes the alternate to be scheduled for computation.
        ValueOrResolvedPtn valueOrResolvedPtn = xRes.caseOf(valueOrPtn, env);
        if (valueOrResolvedPtn != null) {
            Env ptnEnv = xRes.deconstruct(valueOrResolvedPtn, env);
            machine.pushStackEntry(consequent, ptnEnv);
        }
    }

}
