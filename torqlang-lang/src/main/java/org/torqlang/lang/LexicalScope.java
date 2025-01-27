/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.IdentDef;
import org.torqlang.klvm.LocalInstr;
import org.torqlang.klvm.SeqInstr;
import org.torqlang.klvm.Instr;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

public final class LexicalScope {
    private final List<IdentDef> identDefs = new ArrayList<>();
    private final List<Instr> instrs = new ArrayList<>();
    private Instr instr;

    public LexicalScope() {
    }

    public final void addIdentDef(IdentDef identDef) {
        identDefs.add(identDef);
    }

    public final void addInstr(Instr instr) {
        instrs.add(instr);
    }

    final Instr build() {
        if (instr != null) {
            throw new IllegalStateException("Already built error");
        }
        if (instrs.isEmpty()) {
            throw new IllegalStateException("Empty scope");
        }
        if (identDefs.isEmpty()) {
            if (instrs.size() == 1) {
                instr = instrs.get(0);
            } else {
                instr = new SeqInstr(instrs, SourceSpan.adjoin(instrs));
            }
        } else {
            SeqInstr body = new SeqInstr(instrs, SourceSpan.adjoin(instrs));
            instr = new LocalInstr(identDefs, body, SourceSpan.adjoin(instrs));
        }
        return instr;
    }

}
