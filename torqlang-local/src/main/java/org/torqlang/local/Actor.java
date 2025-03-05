/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.PartialField;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.lang.Generator;

public final class Actor {

    public static final Str NEW = Generator.NEW;

    public static ActorBuilderInit builder() {
        return new ActorBuilder();
    }

    public static CompleteRec compileForImport(String source) throws Exception {
        Rec actorRec = builder()
            .setSource(source)
            .construct()
            .actorRec();
        actorRec.checkDetermined();
        PartialField actorField = (PartialField) actorRec.fieldAt(0);
        return Rec.completeRecBuilder()
            .addField(actorField.feature, actorField.value.checkComplete())
            .build();
    }

    public static ActorRef spawn(Address address, ActorImage image) {
        return LocalActor.spawn(address, image);
    }

}
