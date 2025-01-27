/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.torqlang.util.SourceSpan;

public interface BindInstr extends Instr {

    static BindInstr create(CompleteOrIdent leftSide, CompleteOrIdent rightSide, SourceSpan sourceSpan) {
        if (leftSide instanceof Ident lsi) {
            if (rightSide instanceof Ident rsi) {
                return new BindIdentToIdentInstr(rsi, lsi, sourceSpan);
            } else {
                return new BindCompleteToIdentInstr((Complete) rightSide, lsi, sourceSpan);
            }
        } else {
            if (rightSide instanceof Ident rsi) {
                return new BindCompleteToIdentInstr((Complete) leftSide, rsi, sourceSpan);
            } else {
                return new BindCompleteToCompleteInstr((Complete) leftSide, (Complete) rightSide, sourceSpan);
            }
        }
    }

}
