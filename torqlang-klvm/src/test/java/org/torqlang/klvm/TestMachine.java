/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import org.junit.jupiter.api.Test;
import org.torqlang.util.SourceSpan;

import static org.junit.jupiter.api.Assertions.*;

public class TestMachine {

    @Test
    public void testTimeSlicing() {

        // 1 instr and 1 time slice
        Env emptyEnv = Env.emptyEnv();
        Instr skip = new SkipInstr(SourceSpan.emptySourceSpan());
        Stack stack = new Stack(skip, emptyEnv, null);
        Machine machine = new Machine(this, stack);
        ComputeAdvice advice = machine.compute(1);
        assertEquals(ComputeEnd.SINGLETON, advice);
        assertNull(machine.stack()); // Popping 1 instr should leave a null stack
        assertEquals(1, machine.computeCount());
        // Try to compute a program that is ended
        advice = machine.compute(1);
        assertEquals(ComputeEnd.SINGLETON, advice);
        // Total computed should NOT change from previous 1
        assertEquals(1, machine.computeCount());

        // 1 instr and 0 time slice
        emptyEnv = Env.emptyEnv();
        skip = new SkipInstr(SourceSpan.emptySourceSpan());
        stack = new Stack(skip, emptyEnv, null);
        machine = new Machine(this, stack);
        advice = machine.compute(0);
        assertEquals(ComputePreempt.SINGLETON, advice);
        assertNotNull(machine.stack());
        assertEquals(1, machine.stack().size);
        assertNull(machine.stack().next);
        assertEquals(0, machine.computeCount());
        advice = machine.compute(1);
        assertEquals(ComputeEnd.SINGLETON, advice);
        assertNull(machine.stack()); // Popping 1 instr should leave a null stack
        assertEquals(1, machine.computeCount());

        // 3 instrs and 2 time slice
        emptyEnv = Env.emptyEnv();
        stack = new Stack(skip, emptyEnv, null);
        stack = new Stack(skip, emptyEnv, stack);
        stack = new Stack(skip, emptyEnv, stack);
        machine = new Machine(this, stack);
        advice = machine.compute(2);
        assertEquals(ComputePreempt.SINGLETON, advice);
        assertNotNull(machine.stack());
        assertEquals(1, machine.stack().size);
        assertNull(machine.stack().next);
        assertEquals(2, machine.computeCount());
        advice = machine.compute(1);
        assertEquals(ComputeEnd.SINGLETON, advice);
        assertNull(machine.stack());
        assertEquals(3, machine.computeCount());
    }

}
