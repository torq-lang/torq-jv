/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorImage;
import org.torqlang.local.ActorRef;
import org.torqlang.local.Address;

public interface ApiTarget {

    static ApiTarget create(Address address, ActorImage actorImage) {
        return new ApiTargetImage(address, actorImage);
    }

    static ApiTarget create(ActorRef actorRef) {
        return new ApiTargetRef(actorRef);
    }

    Object value();

    final class ApiTargetImage implements ApiTarget {
        public final Address address;
        public final ActorImage actorImage;

        ApiTargetImage(Address address, ActorImage actorImage) {
            this.address = address;
            this.actorImage = actorImage;
        }

        public final Address address() {
            return address;
        }

        @Override
        public final ActorImage value() {
            return actorImage;
        }
    }

    final class ApiTargetRef implements ApiTarget {
        public final ActorRef actorRef;

        ApiTargetRef(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        @Override
        public final ActorRef value() {
            return actorRef;
        }
    }
}
