/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.FailedValue;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Str;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAskConcurrentFeatures {

    @Test
    public void test01() throws Exception {
        String source = """
            actor ConcurrentFeatures() in
                handle ask 'perform' in
                    var f, v
                    var a = {f: v}
                    a = act {'one': 1} end
                    var b = {f: v}
                    b = act {'one': 1} end
                    f = act 'one' end
                    a.one + b.one
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor01"))
            .setSource(source)
            .generate();
        String expected = """
            local $actor_cfgtr in
                $create_actor_cfgtr(proc ($r) in // free vars: $act, $respond
                    local $v0, $v6 in
                        $create_proc(proc ($m) in // free vars: $act, $respond
                            local $else in
                                $create_proc(proc () in // free vars: $m
                                    local $v1 in
                                        local $v2 in
                                            $create_rec({'request': $m}, $v2)
                                            $create_rec('error'#{'name': 'org.torqlang.lang.AskNotHandledError', 'message': 'Actor could not match request message with an \\'ask\\' handler.', 'details': $v2}, $v1)
                                        end
                                        throw $v1
                                    end
                                end, $else)
                                case $m of 'perform' then
                                    local $v3, f, v, a, b in
                                        $create_rec({f: v}, a)
                                        $act
                                            $bind({'one': 1}, a)
                                        end
                                        $create_rec({f: v}, b)
                                        $act
                                            $bind({'one': 1}, b)
                                        end
                                        $act
                                            $bind('one', f)
                                        end
                                        local $v4, $v5 in
                                            $select(a, 'one', $v4)
                                            $select(b, 'one', $v5)
                                            $add($v4, $v5, $v3)
                                        end
                                        $respond($v3)
                                    end
                                else
                                    $else()
                                end
                            end
                        end, $v0)
                        $create_proc(proc ($m) in
                            local $v7 in
                                local $v8 in
                                    $create_rec({'notify': $m}, $v8)
                                    $create_rec('error'#{'name': 'org.torqlang.lang.TellNotHandledError', 'message': 'Actor could not match notify message with a \\'tell\\' handler.', 'details': $v8}, $v7)
                                end
                                throw $v7
                            end
                        end, $v6)
                        $create_tuple('handlers'#[$v0, $v6], $r)
                    end
                end, $actor_cfgtr)
                $create_rec('ConcurrentFeatures'#{'cfg': $actor_cfgtr}, ConcurrentFeatures)
            end""";
        assertEquals(expected, g.createActorRecInstr().toString());
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentFeaturesClient"))
            .send(actorRef, Str.of("perform"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        assertEquals(Int32.I32_2, response);
    }

    @Test
    public void test02() throws Exception {
        /*
         * This source tests a previous deadlock condition caused by interdependent responses, which can occur while
         * bindings records with undetermined features. The "spin waits" for responses to arrive in b, a, v order.
         */
        String sourceWithUndeterminedWaits = """
            actor ConcurrentFeatures() in
                import system.RangeIter
                proc spin_wait(n) in
                    for i in RangeIter.new(0, n) do
                        skip
                    end
                end
                handle ask 'perform' in
                    var a, b, f, v
                    a = {f, v}
                    b = {v, f}
                    b = act {1: 'one'} end
                    a = act spin_wait(20) {'one': 1} end
                    v = act spin_wait(4000) 1 end
                    a.one + 1
                end
            end""";
        String source = """
            actor ConcurrentFeatures() in
                handle ask 'perform' in
                    var a, b, f, v
                    a = {f, v}
                    b = {v, f}
                    b = act {1: 'one'} end
                    a = act {'one': 1} end
                    v = act 1 end
                    a.one + 1
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor02"))
            .setSource(source)
            .generate();
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentFeaturesClient"))
            .send(actorRef, Str.of("perform"))
            .awaitResponse(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        if (response instanceof FailedValue failedValue) {
            System.out.println(failedValue.toDetailsString());
        }
        assertEquals(Int32.I32_2, response);
    }

    @Test
    public void test03() throws Exception {
        String source = """
            actor ConcurrentFeatures() in
                handle ask 'perform' in
                    var a, b, f, v
                    a = {f, v}
                    b = {v, f}
                    v = act 1 end
                    a = act {'one': 1} end
                    b = act {1: 'one'} end
                    a.one + 1
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor03"))
            .setSource(source)
            .generate();
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentFeaturesClient"))
            .send(actorRef, Str.of("perform"))
            .awaitResponse(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        if (response instanceof FailedValue failedValue) {
            System.out.println(failedValue.toDetailsString());
        }
        assertEquals(Int32.I32_2, response);
    }

}
