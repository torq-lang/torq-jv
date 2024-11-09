/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.List;
import java.util.Set;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class DebugStmt extends AbstractStmt {

    public final List<CompleteOrIdent> args;

    public DebugStmt(List<CompleteOrIdent> args, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.args = nullSafeCopyOf(args);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitDebugStmt(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        for (CompleteOrIdent a : args) {
            CompleteOrIdent.captureLexicallyFree(a, knownBound, lexicallyFree);
        }
    }

    @Override
    public final void compute(Env env, Machine machine) {
        System.out.println("Debug point: " + SourceSpan.toLineAndChar(sourceSpan, 1, 1));
        for (int x = 0; x < args.size(); x++) {
            CompleteOrIdent a = args.get(x);
            if (a instanceof Ident ident) {
                Var v = env.get(ident);
                if (v.valueOrVarSet() instanceof Value) {
                    System.out.println("  " + x + ": " + ident + " = " + v.valueOrVarSet());
                } else {
                    VarSet varSet = (VarSet) v.valueOrVarSet();
                    if (varSet.size() > 0) {
                        System.out.println("  " + x + ": " + ident + " = " + v + " = " + varSet);
                    } else {
                        System.out.println("  " + x + ": " + ident + " = " + v);
                    }
                }
            } else {
                System.out.println("  " + x + ": " + a);
            }
        }
    }

}
