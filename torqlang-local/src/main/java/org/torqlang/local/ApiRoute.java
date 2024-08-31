/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

public final class ApiRoute {

    public final ApiPath apiPath;
    public final ApiTarget apiTarget;

    public ApiRoute(ApiPath apiPath, ApiTarget apiTarget) {
        this.apiPath = apiPath;
        this.apiTarget = apiTarget;
    }

    public ApiRoute(ApiPath apiPath, ActorImage actorImage) {
        this(apiPath, ApiTarget.create(actorImage));
    }

    public ApiRoute(ApiPath apiPath, ActorRef actorRef) {
        this(apiPath, ApiTarget.create(actorRef));
    }

}
