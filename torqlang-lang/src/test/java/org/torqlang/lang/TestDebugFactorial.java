/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestDebugFactorial implements DebugInstrListener {

    private final Ident a = Ident.create("a");
    private final Ident x = Ident.create("x");
    private final Ident $r = Ident.$R;
    private final Ident fact = Ident.create("fact");
    private final Ident fact_cps = Ident.create("fact_cps");
    private final Ident n = Ident.create("n");
    private final Ident k = Ident.create("k");
    private final Ident $v0 = Ident.createSystemVarIdent(0);
    private final Ident $v1 = Ident.createSystemVarIdent(1);
    private final Ident $v2 = Ident.createSystemVarIdent(2);
    private final AtomicInteger i = new AtomicInteger(0);

    @Test
    public void test01() throws Exception {
        String source = """
            begin
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
                x = fact(a)
            end""";

        EvaluatorPerformed e = Evaluator.builder()
            .addVar(Ident.create("a"), new Var(Dec128.of("2")))
            .addVar(Ident.create("x"))
            .setDebugInstrListener(this)
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = """
            local fact in
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
                fact(a, x)
            end""";
        assertEquals(expected, e.kernel().toString());
        assertEquals(Dec128.of("2"), e.varAtName("x").valueOrVarSet());
    }

    @Override
    public void onNextInstr(Instr nextInstr, Env nextEnv, Machine machine) {
        int nextIndex = i.getAndIncrement();
        if (nextIndex == 0) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(a).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(x).valueOrVarSet());
            assertInstanceOf(LocalInstr.class, nextInstr);
            // Test incoming Instr
            LocalInstr localInstr = (LocalInstr) nextInstr;
            assertEquals(1, localInstr.xs.size());
            assertEquals(fact, localInstr.xs.get(0).ident);
        } else if (nextIndex == 1) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(a).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(x).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(fact).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(CreateProcInstr.class, nextInstr);
            CreateProcInstr createProcInstr = (CreateProcInstr) nextInstr;
            assertEquals(fact, createProcInstr.x);
        } else if (nextIndex == 2) {
            // Test incoming Env
            assertInstanceOf(Closure.class, nextEnv.get(fact).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(ApplyInstr.class, nextInstr);
            ApplyInstr applyInstr = (ApplyInstr) nextInstr;
            assertEquals(fact, applyInstr.x);
            assertEquals(List.of(a, x), applyInstr.ys);
        } else if (nextIndex == 3) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(x).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(LocalInstr.class, nextInstr);
            LocalInstr localInstr = (LocalInstr) nextInstr;
            assertEquals(fact_cps, localInstr.xs.get(0).ident);
        } else if (nextIndex == 4) {
            // Test incoming Env
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(fact_cps).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(CreateProcInstr.class, nextInstr);
            CreateProcInstr createProcInstr = (CreateProcInstr) nextInstr;
            assertEquals(fact_cps, createProcInstr.x);
        } else if (nextIndex == 5) {
            // Test incoming Env
            assertInstanceOf(Closure.class, nextEnv.get(fact_cps).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(ApplyInstr.class, nextInstr);
            ApplyInstr applyInstr = (ApplyInstr) nextInstr;
            assertEquals(fact_cps, applyInstr.x);
            assertEquals(List.of(x, Dec128.of(1), $r), applyInstr.ys);
        } else if (nextIndex == 6) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(1), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(LocalInstr.class, nextInstr);
            LocalInstr localInstr = (LocalInstr) nextInstr;
            assertEquals(1, localInstr.xs.size());
            assertEquals($v0, localInstr.xs.get(0).ident);
        } else if (nextIndex == 7) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(1), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(LessThanInstr.class, nextInstr);
            LessThanInstr lessThanInstr = (LessThanInstr) nextInstr;
            assertEquals(n, lessThanInstr.a);
            assertEquals(Dec128.of(2), lessThanInstr.b);
            assertEquals($v0, lessThanInstr.x);
        } else if (nextIndex == 8) {
            // Test incoming Env
            assertEquals(Bool.FALSE, nextEnv.get($v0).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(IfElseInstr.class, nextInstr);
            IfElseInstr ifElseInstr = (IfElseInstr) nextInstr;
            assertEquals($v0, ifElseInstr.x);
        } else if (nextIndex == 9) {
            // Test incoming Instr
            assertInstanceOf(LocalInstr.class, nextInstr);
            LocalInstr localInstr = (LocalInstr) nextInstr;
            assertEquals(2, localInstr.xs.size());
            assertEquals($v1, localInstr.xs.get(0).ident);
            assertEquals($v2, localInstr.xs.get(1).ident);
        } else if (nextIndex == 10) {
            // Test incoming Env
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v1).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v2).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(SubtractInstr.class, nextInstr);
            SubtractInstr subtractInstr = (SubtractInstr) nextInstr;
            assertEquals(n, subtractInstr.a);
            assertEquals(Dec128.of(1), subtractInstr.b);
            assertEquals($v1, subtractInstr.x); // $v2 gets bound here
        } else if (nextIndex == 11) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get($v1).valueOrVarSet()); // $sub result is here
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v2).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(MultiplyInstr.class, nextInstr);
            MultiplyInstr multiplyInstr = (MultiplyInstr) nextInstr;
            assertEquals(n, multiplyInstr.a);
            assertEquals(k, multiplyInstr.b);
            assertEquals($v2, multiplyInstr.x);
        } else if (nextIndex == 12) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get($v1).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get($v2).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(ApplyInstr.class, nextInstr);
            ApplyInstr applyInstr = (ApplyInstr) nextInstr;
            assertEquals(fact_cps, applyInstr.x);
            assertEquals(List.of($v1, $v2, $r), applyInstr.ys);
        } else if (nextIndex == 13) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(LocalInstr.class, nextInstr);
            LocalInstr localInstr = (LocalInstr) nextInstr;
            assertEquals(1, localInstr.xs.size());
            assertEquals($v0, localInstr.xs.get(0).ident);
        } else if (nextIndex == 14) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(LessThanInstr.class, nextInstr);
            LessThanInstr lessThanInstr = (LessThanInstr) nextInstr;
            assertEquals(n, lessThanInstr.a);
            assertEquals(Dec128.of(2), lessThanInstr.b);
            assertEquals($v0, lessThanInstr.x);
        } else if (nextIndex == 15) {
            // Test incoming Env
            assertEquals(Bool.TRUE, nextEnv.get($v0).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(IfElseInstr.class, nextInstr);
            IfElseInstr ifElseInstr = (IfElseInstr) nextInstr;
            assertEquals($v0, ifElseInstr.x);
        } else if (nextIndex == 16) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Instr
            assertInstanceOf(BindIdentToIdentInstr.class, nextInstr);
            BindIdentToIdentInstr bindIdentInstr = (BindIdentToIdentInstr) nextInstr;
            assertEquals(k, bindIdentInstr.a);
            assertEquals($r, bindIdentInstr.x);
        } else {
            throw new IllegalStateException("Invalid index: " + nextIndex);
        }
    }

}
