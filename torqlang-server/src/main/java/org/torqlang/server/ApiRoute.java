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

@SuppressWarnings("ClassCanBeRecord")
public final class ApiRoute {

    public final ApiPath path;
    public final ApiTarget target;
    public final ApiDesc desc;
    public final RateLimiter rateLimiter;

    public ApiRoute(ApiPath path, ApiTarget target, ApiDesc desc, RateLimiter rateLimiter) {
        this.path = path;
        this.target = target;
        this.desc = desc;
        this.rateLimiter = rateLimiter;
    }

    public static ApiRoute create(ApiPath path, ActorImage actorImage, ApiDesc desc) {
        return new ApiRoute(path, ApiTarget.create(toAddress(path), actorImage), desc, null);
    }

    public static ApiRoute create(ApiPath path, ActorImage actorImage, ApiDesc desc, RateLimiter rateLimiter) {
        return new ApiRoute(path, ApiTarget.create(toAddress(path), actorImage), desc, rateLimiter);
    }

    public static ApiRoute create(ApiPath path, ActorRef actorRef, ApiDesc desc) {
        return new ApiRoute(path, ApiTarget.create(actorRef), desc, null);
    }

    public static ApiRoute create(ApiPath path, ActorRef actorRef, ApiDesc desc, RateLimiter rateLimiter) {
        return new ApiRoute(path, ApiTarget.create(actorRef), desc, rateLimiter);
    }

    public static Address toAddress(ApiPath path) {
        return Address.create(String.join("/", path.segs));
    }

}
