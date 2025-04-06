/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TestValidatorVars {

    @Test
    public void test01() throws Exception {
        String source = """
            var x""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        TypeEnv typeEnv = TypeEnv.create();
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(typeEnv, alphaVar));
        assertEquals(1, typeEnv.shallowSize());
        assertInstanceOf(VarInfr.class, typeEnv.get(Ident.create("x")));
        assertEquals(ScalarInfr.VOID, subst.get(alphaVar));
    }

    @Test
    public void test02() throws Exception {
        String source = """
            var x, y""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        TypeEnv typeEnv = TypeEnv.create();
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(typeEnv, alphaVar));
        assertEquals(2, typeEnv.shallowSize());
        assertInstanceOf(VarInfr.class, typeEnv.get(Ident.create("x")));
        assertInstanceOf(VarInfr.class, typeEnv.get(Ident.create("y")));
        assertEquals(ScalarInfr.VOID, subst.get(alphaVar));
    }

    @Test
    public void test03() throws Exception {
        String source = """
            var x = 3""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        TypeEnv typeEnv = TypeEnv.create();
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(typeEnv, alphaVar));
        assertEquals(1, typeEnv.shallowSize());
        assertEquals(ScalarInfr.INT32, typeEnv.get(Ident.create("x")));
        assertEquals(ScalarInfr.VOID, subst.get(alphaVar));
    }

    @Test
    public void test04() throws Exception {
        String source = """
            var x = 3, y = 'four'""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        TypeEnv typeEnv = TypeEnv.create();
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(typeEnv, alphaVar));
        assertEquals(2, typeEnv.shallowSize());
        assertEquals(ScalarInfr.INT32, typeEnv.get(Ident.create("x")));
        assertEquals(ScalarInfr.STR, typeEnv.get(Ident.create("y")));
        assertEquals(ScalarInfr.VOID, subst.get(alphaVar));
    }

}
