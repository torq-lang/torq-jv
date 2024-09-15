/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;
import org.torqlang.local.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class HelloWorldBuilder extends AbstractExample {

    public static final boolean TRACE = false;

    public static final String SOURCE_1 = """
        actor HelloWorld(init_name) in
            import system.Cell
            var my_name = Cell.new(init_name)
            handle tell {'name': name} in
                my_name := name
            end
            handle ask 'hello' in
                'Hello, World! My name is ' + @my_name + '.'
            end
        end""";

    public static final String SOURCE_2 = """
        actor PerformHelloWorld() in
            actor HelloWorld(init_name) in
                import system.Cell
                var my_name = Cell.new(init_name)
                handle tell {'name': name} in
                    my_name := name
                end
                handle ask 'hello' in
                    'Hello, World! My name is ' + @my_name + '.'
                end
            end
            handle ask 'perform' in
                var hello_world = spawn(HelloWorld.cfg('Bob'))
                var hello_bob = hello_world.ask('hello')
                hello_world.tell({'name': 'Bobby'})
                var hello_bobby = hello_world.ask('hello')
                [hello_bob, hello_bobby]
            end
        end""";

    public static void main(String[] args) throws Exception {
        new HelloWorldBuilder().performWithErrorCheck();
        System.exit(0);
    }

    @Override
    public final void perform() throws Exception {

        ActorBuilderReady ready = Actor.builder().setSource(SOURCE_1);
        ActorBuilderGenerated generated = ready.generate();
        if (TRACE) {
            System.out.println("========== GENERATED 1 ==========");
            System.out.println(generated.createActorRecStmt());
        }
        ActorBuilderConstructed constructed = generated.construct();
        if (TRACE) {
            System.out.println("========== CONSTRUCTED 1 ==========");
            System.out.println(constructed.actorRec());
        }
        ActorBuilderConfigured configured = constructed.configure(List.of(Str.of("Bob")));
        if (TRACE) {
            System.out.println("========== CONFIGURED 1 ==========");
            System.out.println(configured.actorCfg());
        }
        ActorBuilderSpawned spawned = configured.spawn();
        ActorRef actorRef = spawned.actorRef();

        Object response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("hello"), 100, TimeUnit.MILLISECONDS);
        checkExpectedResponse(Str.of("Hello, World! My name is Bob."), response);

        actorRef.send(Envelope.createNotify(
            Rec.completeRecBuilder()
                .addField(Str.of("name"), Str.of("Bobby"))
                .build()
        ));

        response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("hello"), 100, TimeUnit.MILLISECONDS);
        checkExpectedResponse(Str.of("Hello, World! My name is Bobby."), response);

        ready = Actor.builder().setSource(SOURCE_2);
        generated = ready.generate();
        if (TRACE) {
            System.out.println("========== GENERATED 2 ==========");
            System.out.println(generated.createActorRecStmt());
        }
        constructed = generated.construct();
        if (TRACE) {
            System.out.println("========== CONSTRUCTED 2 ==========");
            System.out.println(constructed.actorRec());
        }
        configured = constructed.configure();
        if (TRACE) {
            System.out.println("========== CONFIGURED 2 ==========");
            System.out.println(configured.actorCfg());
        }
        spawned = configured.spawn();
        actorRef = spawned.actorRef();
        response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("perform"), 100, TimeUnit.MILLISECONDS);
        checkExpectedResponse(
            Rec.completeTupleBuilder()
                .addValue(Str.of("Hello, World! My name is Bob."))
                .addValue(Str.of("Hello, World! My name is Bobby."))
                .build(),
            response);
    }

}
