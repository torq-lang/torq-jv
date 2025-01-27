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

public final class ApiRoute {

    public final ApiPath path;
    public final ApiTarget target;
    public final ApiDesc desc;

    public ApiRoute(ApiPath path, ApiTarget target, ApiDesc desc) {
        this.path = path;
        this.target = target;
        this.desc = desc;
    }

    public ApiRoute(ApiPath path, ActorImage actorImage, ApiDesc desc) {
        this(path, ApiTarget.create(toAddress(path), actorImage), desc);
    }

    public ApiRoute(ApiPath path, Address address, ActorImage actorImage, ApiDesc desc) {
        this(path, ApiTarget.create(address, actorImage), desc);
    }

    public ApiRoute(ApiPath path, ActorRef actorRef, ApiDesc desc) {
        this(path, ApiTarget.create(actorRef), desc);
    }

    public static Address toAddress(ApiPath path) {
        return Address.create(String.join("/", path.segs));
    }

}
