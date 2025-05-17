/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Str;
import org.torqlang.local.Actor;
import org.torqlang.local.ActorRef;
import org.torqlang.local.RequestClient;

import java.util.concurrent.TimeUnit;

public final class ConcurrentMathWithIncr {

    public static final String SOURCE = """
        actor ConcurrentMath() in
            import system.lang.Cell
            actor Number(n) in
                var value = new Cell(n)
                handle ask 'get' in
                    @value
                end
                handle tell 'incr' in
                    value := @value + 1
                end
            end
            var n1 = spawn(new Number(0)),
                n2 = spawn(new Number(0)),
                n3 = spawn(new Number(0))
            handle ask 'calculate' in
                n1.tell('incr')
                n2.tell('incr'); n2.tell('incr')
                n3.tell('incr'); n3.tell('incr'); n3.tell('incr')
                n1.ask('get') + n2.ask('get') * n3.ask('get')
            end
        end""";

    public static void main(String[] args) throws Exception {
        BenchTools.performWithErrorCheck(new ConcurrentMathWithIncr()::perform);
        System.exit(0);
    }

    public final void perform() throws Exception {

        ActorRef actorRef = Actor.builder().spawn(SOURCE).actorRef();

        // 1 + 2 * 3
        Object response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("calculate"), 100, TimeUnit.MILLISECONDS);
        BenchTools.checkExpected(Int32.of(7), response);

        // 2 + 4 * 6
        response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("calculate"), 100, TimeUnit.MILLISECONDS);
        BenchTools.checkExpected(Int32.of(26), response);

        // 3 + 6 * 9
        response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("calculate"), 100, TimeUnit.MILLISECONDS);
        BenchTools.checkExpected(Int32.of(57), response);
    }

}
