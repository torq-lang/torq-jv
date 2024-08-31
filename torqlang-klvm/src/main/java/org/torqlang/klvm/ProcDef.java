/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public final class ProcDef implements Decl {

    public final List<Ident> xs;
    public final Stmt stmt;
    public final SourceSpan sourceSpan;
    public final Set<Ident> freeIdents;

    public ProcDef(List<Ident> xs, Stmt stmt, SourceSpan sourceSpan) {
        this.xs = nullSafeCopyOf(xs);
        this.stmt = stmt;
        this.sourceSpan = sourceSpan;
        HashSet<Ident> kb = new HashSet<>(this.xs);
        HashSet<Ident> lf = new HashSet<>();
        stmt.captureLexicallyFree(kb, lf);
        freeIdents = Set.copyOf(lf);
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state) throws Exception {
        return visitor.visitProcDef(this, state);
    }

    @Override
    public final void captureLexicallyFree(Set<Ident> knownBound, Set<Ident> lexicallyFree) {
        // Free identifiers within the ProcDef were collected in the constructor.
        // Now we must determine free identifiers given a knownBound set.
        for (Ident freeIdent : freeIdents) {
            if (!knownBound.contains(freeIdent)) {
                lexicallyFree.add(freeIdent);
            }
        }
    }

    @Override
    public final String toString() {
        return toKernelString();
    }

}
