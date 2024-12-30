/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

/*
 * Review reactive streams specification.
 *
 * Torq stream lifecycle
 *   1) Create an actor instance
 *      a) An actor type is a publisher if it supports the `handle stream M -> E` signature where M is a message type
 *         and E is an element type
 *      b) Each publisher instantiation is a single-cast server for a stream object
 *      c) A publisher must support the `handle tell 'cancel'` protocol
 *   2) Demand is signaled using `actorRef.stream(M)`
 *      a) The result of `stream` invocation is a StreamRefObj, which is a wrapper for an ActorRef
 *      b) A stream object can be iterated using `ValueIter`
 *      c) A stream object can be queried using `has_more()`
 *         i) This query answers whether `actorRef.stream(M)` may retrieve more elements
 *      d) A stream object can be canceled using `cancel()`
 *      e) TODO: Review reactive streams for how to handle a canceled stream with more elements
 *
 *
 * Source: https://medium.com/@olehdokuka/mastering-own-reactive-streams-implementation-part-1-publisher-e8eaf928a78c
 *
 *   Disclaimer: Rule number 1 of the Reactor club: don’t write your own Publisher. Even though the interface is
 *   simple, the set of rules about interactions between all these reactive streams interface is not.
 *
 *   — Simon Baslé (Pivotal, Project Reactor).
 */
public class StreamRefObj {
}
