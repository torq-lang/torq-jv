/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorImage;
import org.torqlang.local.ActorRef;

public interface ApiReceiver {

    static ApiReceiver create(ActorImage actorImage) {
        return new ApiReceiverImage(actorImage);
    }

    static ApiReceiver create(ActorRef actorRef) {
        return new ApiReceiverRef(actorRef);
    }

    Object value();

    final class ApiReceiverImage implements ApiReceiver {
        public final ActorImage actorImage;

        ApiReceiverImage(ActorImage actorImage) {
            this.actorImage = actorImage;
        }

        @Override
        public final ActorImage value() {
            return actorImage;
        }
    }

    final class ApiReceiverRef implements ApiReceiver {
        public final ActorRef actorRef;

        ApiReceiverRef(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        @Override
        public final ActorRef value() {
            return actorRef;
        }
    }
}
