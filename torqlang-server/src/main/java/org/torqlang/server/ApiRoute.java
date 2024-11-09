/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.ActorImage;
import org.torqlang.local.ActorRef;

public final class ApiRoute {

    public final ApiPath path;
    public final ApiReceiver receiver;
    public final ApiDesc desc;

    public ApiRoute(ApiPath path, ApiReceiver receiver, ApiDesc desc) {
        this.path = path;
        this.receiver = receiver;
        this.desc = desc;
    }

    public ApiRoute(ApiPath path, ActorImage actorImage, ApiDesc desc) {
        this(path, ApiReceiver.create(actorImage), desc);
    }

    public ApiRoute(ApiPath path, ActorRef actorRef, ApiDesc desc) {
        this(path, ApiReceiver.create(actorRef), desc);
    }

}
