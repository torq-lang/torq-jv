/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.Str;
import org.torqlang.local.Actor;
import org.torqlang.local.ActorRef;
import org.torqlang.local.RequestClient;

import java.util.concurrent.TimeUnit;

public final class HelloWorldWithGoodbye extends AbstractExample {

    public static final String SOURCE = """
        actor HelloWorld() in
            handle ask 'hello' in
                'Hello, World!'
            end
            handle ask 'goodbye' in
                'Goodbye, World!'
            end
        end""";

    public static void main(String[] args) throws Exception {
        new HelloWorldWithGoodbye().performWithErrorCheck();
        System.exit(0);
    }

    @Override
    public final void perform() throws Exception {

        ActorRef actorRef = Actor.builder().spawn(SOURCE).actorRef();

        Object response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("hello"), 100, TimeUnit.MILLISECONDS);

        checkExpectedResponse(Str.of("Hello, World!"), response);

        response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("goodbye"), 100, TimeUnit.MILLISECONDS);

        checkExpectedResponse(Str.of("Goodbye, World!"), response);
    }

}
