/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

/*
 * Protocols are not values. They are simply blueprints for message passing, blueprints for handling ask, tell, and
 * stream messages. Protocols can be used to describe and type check message interactions.
 *
 * There are three types that can be constrained by protocols:
 *   1. Actor Configurator -- ActorCfgtr[P <: Protocol]
 *   2. Actor Configuration -- ActorCfg[P <: Protocol]
 *   3. Actor Reference -- ActorRef[P <: Protocol]
 */
public interface Protocol extends Lang {
}
