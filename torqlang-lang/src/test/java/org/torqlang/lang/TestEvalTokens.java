/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;
import org.torqlang.klvm.Token;
import org.torqlang.klvm.TokenPack;
import org.torqlang.klvm.Var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TestEvalTokens {

    @Test
    public void test() throws Exception {
        String source = """
            begin
                x = Token.new()
            end""";
        EvaluatorPerformed e = Evaluator.builder()
            .addVar(TokenPack.TOKEN_IDENT, new Var(TokenPack.TOKEN_CLS))
            .addVar(Ident.create("x"))
            .setSource(source)
            .perform();
        assertEquals(source, e.stmtOrExpr().toString());
        String expected = "$select_apply(Token, ['new'], x)";
        assertEquals(expected, e.kernel().toString());
        assertInstanceOf(Token.class, e.varAtName("x").valueOrVarSet());
    }

}
