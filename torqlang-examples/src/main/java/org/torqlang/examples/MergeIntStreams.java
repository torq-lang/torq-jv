/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.*;
import org.torqlang.local.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MergeIntStreams {

    public static final String SOURCE = """
        actor MergeIntStreams() in
            import system.{ArrayList, Cell, Stream, ValueIter}
            import examples.IntPublisher
            handle ask 'merge' in
                var odd_iter = new ValueIter(new Stream(spawn(new IntPublisher(1, 10, 2)), 'request'#{'count': 3})),
                    even_iter = new ValueIter(new Stream(spawn(new IntPublisher(2, 10, 2)), 'request'#{'count': 2}))
                var answer = new ArrayList()
                var odd_next = new Cell(odd_iter()),
                    even_next = new Cell(even_iter())
                while @odd_next != eof && @even_next != eof do
                    if (@odd_next < @even_next) then
                        answer.add(@odd_next)
                        odd_next := odd_iter()
                    else
                        answer.add(@even_next)
                        even_next := even_iter()
                    end
                end
                while @odd_next != eof do
                    answer.add(@odd_next)
                    odd_next := odd_iter()
                end
                while @even_next != eof do
                    answer.add(@even_next)
                    even_next := even_iter()
                end
                answer.to_array()
            end
        end""";

    public static void main(String[] args) throws Exception {
        BenchTools.performWithErrorCheck(new MergeIntStreams()::perform);
        System.exit(0);
    }

    public final void perform() throws Exception {

        ActorSystem system = ActorSystem.builder()
            .addDefaultModules()
            .addModule("examples", IntPublisherMod.moduleRec())
            .build();

        ActorRef actorRef = Actor.builder()
            .setSystem(system)
            .spawn(SOURCE).actorRef();

        Object response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("merge"), 100, TimeUnit.MILLISECONDS);

        List<?> expectedTuple = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        BenchTools.checkExpected(expectedTuple, ValueTools.toNativeValue((Complete) response));
    }

}
