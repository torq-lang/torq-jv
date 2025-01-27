/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class Machine {

    private final Object owner;

    private Stack stack;
    private Stack current;
    private long computeCount;

    public Machine(Object owner, Stack stack) {
        this(owner, stack, 0);
    }

    public Machine(Object owner, Stack stack, long computeCount) {
        this.owner = owner;
        this.stack = stack;
        this.computeCount = computeCount;
    }

    public static void compute(Object owner, Stack stack, long timeSlice) {
        Machine machine = new Machine(owner, stack);
        ComputeAdvice advice = machine.compute(timeSlice);
        while (advice == ComputePreempt.SINGLETON) {
            advice = machine.compute(timeSlice);
        }
        if (advice.isWait()) {
            throw new IllegalStateException("Machine is waiting on a variable");
        }
        if (advice.isHalt()) {
            throw new MachineHaltError((ComputeHalt) advice);
        }
    }

    public final ComputeAdvice compute(long timeSlice) {
        if (stack == null) {
            return ComputeEnd.SINGLETON;
        }
        long computeAllowed = computeCount + timeSlice;
        while (computeCount < computeAllowed) {
            computeCount++;
            current = stack;
            stack = stack.next;
            try {
                current.instr.compute(current.env, this);
            } catch (WaitException wx) {
                stack = current;
                current = null;
                return new ComputeWait(wx.barrier());
            } catch (NativeThrow nt) {
                ThrowInstr ti = new ThrowInstr(nt.error, nt, current.instr);
                stack = new Stack(ti, current.env, current);
            } catch (MachineError error) {
                return error.asComputeHalt(current);
            } catch (Throwable throwable) {
                Complete ne = new NativeError(throwable);
                ThrowInstr ti = new ThrowInstr(ne, throwable, current.instr);
                stack = new Stack(ti, current.env, current);
            }
            if (stack == null) {
                // INVARIANT: Even though we completed the computation, the field 'current' must hold the last
                // instruction computed.
                return ComputeEnd.SINGLETON;
            }
        }
        return ComputePreempt.SINGLETON;
    }

    public final long computeCount() {
        return computeCount;
    }

    public final Stack current() {
        return current;
    }

    @SuppressWarnings("unchecked")
    public final <T> T owner() {
        return (T) owner;
    }

    final Stack popStackEntry() {
        Stack entry = stack;
        if (stack != null) {
            stack = stack.next;
        }
        return entry;
    }

    public final void pushStackEntries(InstrList instrList, Env env) {
        for (InstrList.Entry current = instrList.lastEntry(); current != null; current = current.prev()) {
            stack = new Stack(current.instr(), env, stack);
        }
    }

    public final void pushStackEntry(Instr instr, Env env) {
        stack = new Stack(instr, env, stack);
    }

    public final Stack stack() {
        return stack;
    }

    final void unwindToJumpCatchInstr(JumpThrowInstr jumpThrowInstr) {
        int jumpThrowId = jumpThrowInstr.id;
        while (stack != null) {
            if (stack.instr instanceof JumpCatchInstr jumpCatchInstr && jumpCatchInstr.id == jumpThrowId) {
                break;
            }
            stack = stack.next;
        }
        if (stack == null) {
            // If this condition occurs, we generated an invalid program containing unmatched jump-throw/jump-catch
            // instructions. The field 'current' will hold the instruction that issued the unmatched jump-throw.
            throw new UnmatchedJumpThrowError(jumpThrowInstr);
        }
    }

    final void unwindToNextCatchInstr(Complete error, Throwable nativeCause) {
        while (stack != null) {
            if (stack.instr instanceof CatchInstr catchInstr) {
                Env catchEnv = Env.createPrivatelyForKlvm(stack.env,
                    new EnvEntry[]{new EnvEntry(catchInstr.arg, new Var(error))});
                stack = new Stack(catchInstr.caseInstr, catchEnv, stack.next);
                break;
            }
            stack = stack.next;
        }
        if (stack == null) {
            // INVARIANT: Even though we have unwound the stack, the field 'current' still holds the
            // instruction that threw the error.
            throw new UncaughtThrowError(error, nativeCause);
        }
    }

}
