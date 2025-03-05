/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.CompleteTuple;
import org.torqlang.klvm.Int32;
import org.torqlang.klvm.Rec;
import org.torqlang.klvm.Str;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAskConcurrentData {

    @Test
    public void test01() throws Exception {
        String source = """
            actor ConcurrentData() in
                handle ask 'perform' in
                    {'customer': act 'Alice and Bob' end, 'order': act '20 pounds of Sugar' end}
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor"))
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
                                    local $v3 in
                                        local $v4, $v5 in
                                            $act
                                                $bind('Alice and Bob', $v4)
                                            end
                                            $act
                                                $bind('20 pounds of Sugar', $v5)
                                            end
                                            $create_rec({'customer': $v4, 'order': $v5}, $v3)
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
                $create_rec('ConcurrentData'#{'new': $actor_cfgtr}, ConcurrentData)
            end""";
        assertEquals(expected, g.createActorRecInstr().toString());
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentDataClient"))
            .send(actorRef, Str.of("perform"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        CompleteRec expectedRec = Rec.completeRecBuilder()
            .addField(Str.of("customer"), Str.of("Alice and Bob"))
            .addField(Str.of("order"), Str.of("20 pounds of Sugar"))
            .build();
        assertEquals(expectedRec, response);
    }

    @Test
    public void test02() throws Exception {
        String source = """
            actor ConcurrentData() in
                import system.ArrayList
                handle ask 'perform' in
                    var list = new ArrayList()
                    list.add({'customer': act 'Alice and Bob' end, 'order': act '20 pounds of Sugar' end})
                    list.add({'customer': act 'Charles and Debbie' end, 'order': act '50 pounds of Flour' end})
                    list.to_tuple()
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor"))
            .setSource(source)
            .generate();
        String expected = """
            local $actor_cfgtr in
                $create_actor_cfgtr(proc ($r) in // free vars: $act, $import, $respond
                    local ArrayList, $v0, $v10 in
                        $import('system', ['ArrayList'])
                        $create_proc(proc ($m) in // free vars: $act, $respond, ArrayList
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
                                    local $v3, list in
                                        $select_apply(ArrayList, ['new'], list)
                                        local $v4 in
                                            local $v5, $v6 in
                                                $act
                                                    $bind('Alice and Bob', $v5)
                                                end
                                                $act
                                                    $bind('20 pounds of Sugar', $v6)
                                                end
                                                $create_rec({'customer': $v5, 'order': $v6}, $v4)
                                            end
                                            $select_apply(list, ['add'], $v4)
                                        end
                                        local $v7 in
                                            local $v8, $v9 in
                                                $act
                                                    $bind('Charles and Debbie', $v8)
                                                end
                                                $act
                                                    $bind('50 pounds of Flour', $v9)
                                                end
                                                $create_rec({'customer': $v8, 'order': $v9}, $v7)
                                            end
                                            $select_apply(list, ['add'], $v7)
                                        end
                                        $select_apply(list, ['to_tuple'], $v3)
                                        $respond($v3)
                                    end
                                else
                                    $else()
                                end
                            end
                        end, $v0)
                        $create_proc(proc ($m) in
                            local $v11 in
                                local $v12 in
                                    $create_rec({'notify': $m}, $v12)
                                    $create_rec('error'#{'name': 'org.torqlang.lang.TellNotHandledError', 'message': 'Actor could not match notify message with a \\'tell\\' handler.', 'details': $v12}, $v11)
                                end
                                throw $v11
                            end
                        end, $v10)
                        $create_tuple('handlers'#[$v0, $v10], $r)
                    end
                end, $actor_cfgtr)
                $create_rec('ConcurrentData'#{'new': $actor_cfgtr}, ConcurrentData)
            end""";
        assertEquals(expected, g.createActorRecInstr().toString());
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentDataClient"))
            .send(actorRef, Str.of("perform"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        CompleteTuple expectedTuple = Rec.completeTupleBuilder()
            .addValue(
                Rec.completeRecBuilder()
                    .addField(Str.of("customer"), Str.of("Alice and Bob"))
                    .addField(Str.of("order"), Str.of("20 pounds of Sugar"))
                    .build()
            )
            .addValue(
                Rec.completeRecBuilder()
                    .addField(Str.of("customer"), Str.of("Charles and Debbie"))
                    .addField(Str.of("order"), Str.of("50 pounds of Flour"))
                    .build()
            )
            .build();
        assertEquals(expectedTuple, response);
    }

    @Test
    public void test03() throws Exception {
        String source = """
            actor ConcurrentMathTuple() in
                actor Number(n) in
                    handle ask 'get' in
                        n
                    end
                end
                var n1 = spawn(new Number(1)),
                    n2 = spawn(new Number(2)),
                    n3 = spawn(new Number(3))
                handle ask 'construct' in
                    [n1.ask('get'), n2.ask('get'), n3.ask('get')]
                end
            end""";
        ActorBuilderGenerated g = Actor.builder()
            .setAddress(Address.create(getClass().getName() + "Actor"))
            .setSource(source)
            .generate();
        String expected = """
            local $actor_cfgtr in
                $create_actor_cfgtr(proc ($r) in // free vars: $respond, $spawn
                    local Number, $actor_cfgtr, n1, n2, n3, $v10, $v17 in
                        $create_actor_cfgtr(proc (n, $r) in // free vars: $respond
                            local $v0, $v4 in
                                $create_proc(proc ($m) in // free vars: $respond, n
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
                                        case $m of 'get' then
                                            local $v3 in
                                                $bind(n, $v3)
                                                $respond($v3)
                                            end
                                        else
                                            $else()
                                        end
                                    end
                                end, $v0)
                                $create_proc(proc ($m) in
                                    local $v5 in
                                        local $v6 in
                                            $create_rec({'notify': $m}, $v6)
                                            $create_rec('error'#{'name': 'org.torqlang.lang.TellNotHandledError', 'message': 'Actor could not match notify message with a \\'tell\\' handler.', 'details': $v6}, $v5)
                                        end
                                        throw $v5
                                    end
                                end, $v4)
                                $create_tuple('handlers'#[$v0, $v4], $r)
                            end
                        end, $actor_cfgtr)
                        $create_rec('Number'#{'new': $actor_cfgtr}, Number)
                        local $v7 in
                            $select_apply(Number, ['new'], 1, $v7)
                            $spawn($v7, n1)
                        end
                        local $v8 in
                            $select_apply(Number, ['new'], 2, $v8)
                            $spawn($v8, n2)
                        end
                        local $v9 in
                            $select_apply(Number, ['new'], 3, $v9)
                            $spawn($v9, n3)
                        end
                        $create_proc(proc ($m) in // free vars: $respond, n1, n2, n3
                            local $else in
                                $create_proc(proc () in // free vars: $m
                                    local $v11 in
                                        local $v12 in
                                            $create_rec({'request': $m}, $v12)
                                            $create_rec('error'#{'name': 'org.torqlang.lang.AskNotHandledError', 'message': 'Actor could not match request message with an \\'ask\\' handler.', 'details': $v12}, $v11)
                                        end
                                        throw $v11
                                    end
                                end, $else)
                                case $m of 'construct' then
                                    local $v13 in
                                        local $v14, $v15, $v16 in
                                            $select_apply(n1, ['ask'], 'get', $v14)
                                            $select_apply(n2, ['ask'], 'get', $v15)
                                            $select_apply(n3, ['ask'], 'get', $v16)
                                            $create_tuple([$v14, $v15, $v16], $v13)
                                        end
                                        $respond($v13)
                                    end
                                else
                                    $else()
                                end
                            end
                        end, $v10)
                        $create_proc(proc ($m) in
                            local $v18 in
                                local $v19 in
                                    $create_rec({'notify': $m}, $v19)
                                    $create_rec('error'#{'name': 'org.torqlang.lang.TellNotHandledError', 'message': 'Actor could not match notify message with a \\'tell\\' handler.', 'details': $v19}, $v18)
                                end
                                throw $v18
                            end
                        end, $v17)
                        $create_tuple('handlers'#[$v10, $v17], $r)
                    end
                end, $actor_cfgtr)
                $create_rec('ConcurrentMathTuple'#{'new': $actor_cfgtr}, ConcurrentMathTuple)
            end""";
        assertEquals(expected, g.createActorRecInstr().toString());
        ActorRef actorRef = g.spawn().actorRef();
        Object response = RequestClient.builder()
            .setAddress(Address.create("ConcurrentDataClient"))
            .send(actorRef, Str.of("construct"))
            .awaitResponse(100, TimeUnit.MILLISECONDS);
        CompleteTuple expectedTuple = Rec.completeTupleBuilder()
            .addValue(Int32.of(1))
            .addValue(Int32.of(2))
            .addValue(Int32.of(3))
            .build();
        assertEquals(expectedTuple, response);
    }

}
