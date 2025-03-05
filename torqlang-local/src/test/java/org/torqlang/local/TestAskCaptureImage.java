/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestAskCaptureImage {

    @Test
    public void testConcurrentHelloFactorials() throws Exception {

        // This example captures a function closure as part of the actor image

        String source = """
            actor HelloConcurrentFactorials() in
                func fact(x) in
                    func fact_cps(n, k) in
                        if n < 2m then
                            k
                        else
                            fact_cps(n - 1m, n * k)
                        end
                    end
                    fact_cps(x, 1m)
                end
                handle ask {'hello': num} in
                    ['Hello, ' + num + '! is ' + fact(num), 'Hello, ' + (num + 1m) + '! is ' + fact(num + 1m)]
                end
            end""";

        ActorImage actorImage = Actor.builder().actorImage(source);

        ActorRef actorRef = Actor.spawn(Address.create(getClass().getName() + "Actor"), actorImage);

        CompleteRec m = Rec.completeRecBuilder().addField(Str.of("hello"), Dec128.of(10)).build();
        Object response = RequestClient.builder()
            .setAddress(Address.create("HelloConcurrentFactorials"))
            .send(actorRef, m)
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        List<String> expected = List.of("Hello, 10! is 3628800", "Hello, 11! is 39916800");
        assertEquals(expected, ValueTools.toNativeValue((Complete) response));
    }

    @Test
    public void testHelloFactorials() throws Exception {

        // This example captures a function closure as part of the actor image

        String source = """
            actor HelloFactorial() in
                func fact(x) in
                    func fact_cps(n, k) in
                        if n < 2m then
                            k
                        else
                            fact_cps(n - 1m, n * k)
                        end
                    end
                    fact_cps(x, 1m)
                end
                handle ask {'hello': num} in
                    'Hello, ' + num + '! is ' + fact(num)
                end
            end""";

        ActorImage actorImage = Actor.builder().actorImage(source);

        ActorRef actorRef = Actor.spawn(Address.create(getClass().getName() + "Actor"), actorImage);

        CompleteRec m = Rec.completeRecBuilder().addField(Str.of("hello"), Dec128.of(10)).build();
        Object response = RequestClient.builder()
            .setAddress(Address.create("HelloFactorialClient"))
            .send(actorRef, m)
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        assertEquals(Str.of("Hello, 10! is 3628800"), response);
    }

    @Test
    public void testHelloWorld() throws Exception {

        String source = """
            actor HelloWorld() in
                handle ask 'hello' in
                    'Hello, World!'
                end
            end""";

        ActorImage actorImage = Actor.builder().actorImage(source);

        ActorRef actorRef = Actor.spawn(Address.create(getClass().getName() + "Actor"), actorImage);

        Object response = RequestClient.builder()
            .setAddress(Address.create("HelloWorldClient"))
            .send(actorRef, Str.of("hello"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        assertEquals(Str.of("Hello, World!"), response);

        // Perform it a second time
        response = RequestClient.builder()
            .setAddress(Address.create("HelloWorldClient"))
            .send(actorRef, Str.of("hello"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        assertEquals(Str.of("Hello, World!"), response);
    }

    @Test
    public void testMutableNotAllowed() {

        // This example captures a function closure as part of the actor image

        String source = """
            actor HelloConcurrentFactorials() in
                import system.Cell
                var two = new Cell(2m)
                func fact(x) in
                    func fact_cps(n, k) in
                        if n < two then
                            k
                        else
                            fact_cps(n - 1m, n * k)
                        end
                    end
                    fact_cps(x, 1m)
                end
                handle ask {'hello': num} in
                    ['Hello, ' + num + '! is ' + fact(num), 'Hello, ' + (num + 1m) + '! is ' + fact(num + 1m)]
                end
            end""";

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Actor.builder().actorImage(source));
        assertTrue(exception.getMessage().contains("Cannot complete"));
    }

}
