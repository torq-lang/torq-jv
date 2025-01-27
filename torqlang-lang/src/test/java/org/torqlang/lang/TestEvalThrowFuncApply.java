/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.MachineHaltError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestEvalThrowFuncApply {

    @Test
    public void testThrowFuncExpr() throws Exception {
        String source = """
            begin
                func create_error(m) in
                    {'message': m}
                end
                throw create_error('test')
            end""";
        EvaluatorGenerated g = Evaluator.builder()
            .setSource(source)
            .generate();
        String expected = """
            begin
                func create_error(m) in
                    {'message': m}
                end
                throw create_error('test')
            end""";
        assertEquals(expected, g.stmtOrExpr().toString());
        expected = """
            local create_error in
                $create_proc(proc (m, $r) in
                    $create_rec({'message': m}, $r)
                end, create_error)
                local $v0 in
                    create_error('test', $v0)
                    throw $v0
                end
            end""";
        assertEquals(expected, g.kernel().toString());
        MachineHaltError e = assertThrows(MachineHaltError.class, g::perform);
        assertEquals("{'message': 'test'}", e.getMessage());
    }

}
