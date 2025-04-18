/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.ActorCfg;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Str;
import org.torqlang.klvm.Var;

import static org.junit.jupiter.api.Assertions.*;

public class TestEvalActor {

    @Test
    public void test() throws Exception {
        String source = """
            begin
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
                end
                hello_factorial_cfg = HelloFactorial.cfg()
            end""";
        Ident configCtorIdent = Ident.create("hello_factorial_cfg");
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.$RESPOND, new Var(Str.of("RESPOND_PROC_GOES_HERE")))
            .addVar(configCtorIdent)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local HelloFactorial, $actor_cfgtr in
                $create_actor_cfgtr(proc ($r) in // free vars: $respond
                    local fact, $v3, $v10 in
                        $create_proc(proc (x, $r) in
                            local fact_cps in
                                $create_proc(proc (n, k, $r) in // free vars: fact_cps
                                    local $v0 in
                                        $lt(n, 2m, $v0)
                                        if $v0 then
                                            $bind(k, $r)
                                        else
                                            local $v1, $v2 in
                                                $sub(n, 1m, $v1)
                                                $mult(n, k, $v2)
                                                fact_cps($v1, $v2, $r)
                                            end
                                        end
                                    end
                                end, fact_cps)
                                fact_cps(x, 1m, $r)
                            end
                        end, fact)
                        $create_proc(proc ($m) in // free vars: $respond, fact
                            local $else in
                                $create_proc(proc () in // free vars: $m
                                    local $v4 in
                                        local $v5 in
                                            $create_rec({'request': $m}, $v5)
                                            $create_rec('error'#{'name': 'org.torqlang.lang.AskNotHandledError', 'message': 'Actor could not match request message with an \\'ask\\' handler.', 'details': $v5}, $v4)
                                        end
                                        throw $v4
                                    end
                                end, $else)
                                case $m of {'hello': num} then
                                    local $v6 in
                                        local $v7, $v9 in
                                            local $v8 in
                                                $add('Hello, ', num, $v8)
                                                $add($v8, '! is ', $v7)
                                            end
                                            fact(num, $v9)
                                            $add($v7, $v9, $v6)
                                        end
                                        $respond($v6)
                                    end
                                else
                                    $else()
                                end
                            end
                        end, $v3)
                        $create_proc(proc ($m) in
                            local $v11 in
                                local $v12 in
                                    $create_rec({'notify': $m}, $v12)
                                    $create_rec('error'#{'name': 'org.torqlang.lang.TellNotHandledError', 'message': 'Actor could not match notify message with a \\'tell\\' handler.', 'details': $v12}, $v11)
                                end
                                throw $v11
                            end
                        end, $v10)
                        $create_tuple('handlers'#[$v3, $v10], $r)
                    end
                end, $actor_cfgtr)
                $create_rec('HelloFactorial'#{'cfg': $actor_cfgtr}, HelloFactorial)
                $select_apply(HelloFactorial, ['cfg'], hello_factorial_cfg)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(ActorCfg.class, e.varAtName(configCtorIdent.name).valueOrVarSet());
    }

}
