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

public final class ConcurrentMath {

    public static final String SOURCE = """
        actor ConcurrentMath() in
            actor Number(n) in
                handle ask 'get' in
                    n
                end
            end
            var n1 = spawn(new Number(1)),
                n2 = spawn(new Number(2)),
                n3 = spawn(new Number(3))
            handle ask 'calculate' in
                n1.ask('get') + n2.ask('get') * n3.ask('get')
            end
        end""";

    public static void main(String[] args) throws Exception {
        BenchTools.performWithErrorCheck(new ConcurrentMath()::perform);
        System.exit(0);
    }

    public final void perform() throws Exception {

        ActorRef actorRef = Actor.builder().spawn(SOURCE).actorRef();

        Object response = RequestClient.builder()
            .sendAndAwaitResponse(actorRef, Str.of("calculate"), 100, TimeUnit.MILLISECONDS);
        BenchTools.checkExpected(Int32.of(7), response);
    }

}
