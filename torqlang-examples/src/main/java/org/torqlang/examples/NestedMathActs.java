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
import org.torqlang.local.Address;
import org.torqlang.local.RequestClient;

import java.util.concurrent.TimeUnit;

public final class NestedMathActs extends AbstractExample {

    public static final String SOURCE = """
        actor NestedMathActs() in
            handle ask 'calculate' in
                var a, b, c, d
                a = act b + c + act d + 11 end end
                c = act b + d end
                d = act 5 end
                b = 7
                a
            end
        end""";

    public static void main(String[] args) throws Exception {
        new NestedMathActs().performWithErrorCheck();
        System.exit(0);
    }

    @Override
    public final void perform() throws Exception {

        ActorRef actorRef = Actor.builder()
            .setAddress(Address.create(NestedMathActs.class.getName()))
            .setSource(SOURCE)
            .spawn()
            .actorRef();

        Object response = RequestClient.builder()
            .setAddress(Address.create("NestedMathClient"))
            .send(actorRef, Str.of("calculate"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);

        checkExpectedResponse(Int32.of(35), response);
    }

}
