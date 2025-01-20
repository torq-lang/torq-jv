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

public class TestDebugFactorial implements DebugStmtListener {

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
            .setDebugStmtListener(this)
            .setSource(source)
            .perform();
        assertEquals(source, e.sntcOrExpr().toString());
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
    public void onDebugStmt(DebugStmt stmt, Machine machine) {
        int nextIndex = i.getAndIncrement();
        Env nextEnv = stmt.nextEnv();
        Stmt nextStmt = stmt.nextStmt();
        if (nextIndex == 0) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(a).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(x).valueOrVarSet());
            assertInstanceOf(LocalStmt.class, nextStmt);
            // Test incoming Stmt
            LocalStmt localStmt = (LocalStmt) nextStmt;
            assertEquals(1, localStmt.xs.size());
            assertEquals(fact, localStmt.xs.get(0).ident);
        } else if (nextIndex == 1) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(a).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(x).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(fact).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(CreateProcStmt.class, nextStmt);
            CreateProcStmt createProcStmt = (CreateProcStmt) nextStmt;
            assertEquals(fact, createProcStmt.x);
        } else if (nextIndex == 2) {
            // Test incoming Env
            assertInstanceOf(Closure.class, nextEnv.get(fact).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(ApplyStmt.class, nextStmt);
            ApplyStmt applyStmt = (ApplyStmt) nextStmt;
            assertEquals(fact, applyStmt.x);
            assertEquals(List.of(a, x), applyStmt.ys);
        } else if (nextIndex == 3) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(x).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(LocalStmt.class, nextStmt);
            LocalStmt localStmt = (LocalStmt) nextStmt;
            assertEquals(fact_cps, localStmt.xs.get(0).ident);
        } else if (nextIndex == 4) {
            // Test incoming Env
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get(fact_cps).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(CreateProcStmt.class, nextStmt);
            CreateProcStmt createProcStmt = (CreateProcStmt) nextStmt;
            assertEquals(fact_cps, createProcStmt.x);
        } else if (nextIndex == 5) {
            // Test incoming Env
            assertInstanceOf(Closure.class, nextEnv.get(fact_cps).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(ApplyStmt.class, nextStmt);
            ApplyStmt applyStmt = (ApplyStmt) nextStmt;
            assertEquals(fact_cps, applyStmt.x);
            assertEquals(List.of(x, Dec128.of(1), $r), applyStmt.ys);
        } else if (nextIndex == 6) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(1), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(LocalStmt.class, nextStmt);
            LocalStmt localStmt = (LocalStmt) nextStmt;
            assertEquals(1, localStmt.xs.size());
            assertEquals($v0, localStmt.xs.get(0).ident);
        } else if (nextIndex == 7) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(1), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(LessThanStmt.class, nextStmt);
            LessThanStmt lessThanStmt = (LessThanStmt) nextStmt;
            assertEquals(n, lessThanStmt.a);
            assertEquals(Dec128.of(2), lessThanStmt.b);
            assertEquals($v0, lessThanStmt.x);
        } else if (nextIndex == 8) {
            // Test incoming Env
            assertEquals(Bool.FALSE, nextEnv.get($v0).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(IfElseStmt.class, nextStmt);
            IfElseStmt ifElseStmt = (IfElseStmt) nextStmt;
            assertEquals($v0, ifElseStmt.x);
        } else if (nextIndex == 9) {
            // Test incoming Stmt
            assertInstanceOf(LocalStmt.class, nextStmt);
            LocalStmt localStmt = (LocalStmt) nextStmt;
            assertEquals(2, localStmt.xs.size());
            assertEquals($v1, localStmt.xs.get(0).ident);
            assertEquals($v2, localStmt.xs.get(1).ident);
        } else if (nextIndex == 10) {
            // Test incoming Env
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v1).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v2).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(SubtractStmt.class, nextStmt);
            SubtractStmt subtractStmt = (SubtractStmt) nextStmt;
            assertEquals(n, subtractStmt.a);
            assertEquals(Dec128.of(1), subtractStmt.b);
            assertEquals($v1, subtractStmt.x); // $v2 gets bound here
        } else if (nextIndex == 11) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get($v1).valueOrVarSet()); // $sub result is here
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($v2).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(MultiplyStmt.class, nextStmt);
            MultiplyStmt multiplyStmt = (MultiplyStmt) nextStmt;
            assertEquals(n, multiplyStmt.a);
            assertEquals(k, multiplyStmt.b);
            assertEquals($v2, multiplyStmt.x);
        } else if (nextIndex == 12) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get($v1).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get($v2).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(ApplyStmt.class, nextStmt);
            ApplyStmt applyStmt = (ApplyStmt) nextStmt;
            assertEquals(fact_cps, applyStmt.x);
            assertEquals(List.of($v1, $v2, $r), applyStmt.ys);
        } else if (nextIndex == 13) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(LocalStmt.class, nextStmt);
            LocalStmt localStmt = (LocalStmt) nextStmt;
            assertEquals(1, localStmt.xs.size());
            assertEquals($v0, localStmt.xs.get(0).ident);
        } else if (nextIndex == 14) {
            // Test incoming Env
            assertEquals(Dec128.of(1), nextEnv.get(n).valueOrVarSet());
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(LessThanStmt.class, nextStmt);
            LessThanStmt lessThanStmt = (LessThanStmt) nextStmt;
            assertEquals(n, lessThanStmt.a);
            assertEquals(Dec128.of(2), lessThanStmt.b);
            assertEquals($v0, lessThanStmt.x);
        } else if (nextIndex == 15) {
            // Test incoming Env
            assertEquals(Bool.TRUE, nextEnv.get($v0).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(IfElseStmt.class, nextStmt);
            IfElseStmt ifElseStmt = (IfElseStmt) nextStmt;
            assertEquals($v0, ifElseStmt.x);
        } else if (nextIndex == 16) {
            // Test incoming Env
            assertEquals(Dec128.of(2), nextEnv.get(k).valueOrVarSet());
            assertSame(VarSet.EMPTY_VAR_SET, nextEnv.get($r).valueOrVarSet());
            // Test incoming Stmt
            assertInstanceOf(BindIdentToIdentStmt.class, nextStmt);
            BindIdentToIdentStmt bindIdentStmt = (BindIdentToIdentStmt) nextStmt;
            assertEquals(k, bindIdentStmt.a);
            assertEquals($r, bindIdentStmt.x);
        } else {
            throw new IllegalStateException("Invalid index: " + nextIndex);
        }
    }

}
