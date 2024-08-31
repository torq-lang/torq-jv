/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

final class BasicCompleteRec extends AbstractCompleteRec {

    private BasicCompleteRec() {
    }

    private BasicCompleteRec(Literal label, CompleteField[] completeFields) {
        restore(label, completeFields);
    }

    static CompleteRec createPrivatelyForKlvm(Literal label, List<CompleteField> completeFields) {
        return new BasicCompleteRec(label, completeFields.toArray(new CompleteField[0]));
    }

    static BasicCompleteRec instanceForRestore() {
        return new BasicCompleteRec();
    }

    @Override
    public final int unificationPriority() {
        return UnificationPriority.COMPLETE_REC;
    }

}
