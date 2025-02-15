/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestValidate {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                var x = 3
                var y = 5
                x + y
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarType.INT32, subst.get(alphaVar));
    }

//    @Test
// TODO: Validator.visitIdentVarDecl() needs impl
    public void test02() throws Exception {
        String source = """
            begin
                var x::Int32
                var y::Int32
                x + y
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarType.INT32, subst.get(alphaVar));
    }

    @Test
    public void test03() throws Exception {
        String source = """
            begin
                var x = 3
                var y = 'five'
                x + y
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class, () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 begin
            0002     var x = 3
            0003     var y = 'five'
            0004     x + y
                         ^__ Expected Int32 but found Str
            0005 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test04() throws Exception {
        String source = """
            begin
                var Int32 = 3
                var y = 5
                x + y
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        IllegalIdentError error = assertThrows(IllegalIdentError.class, () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 begin
            0002     var Int32 = 3
                         ^__ Illegal identifier
            0003     var y = 5
            0004     x + y
            0005 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test97() throws Exception {
        // TODO: Add test failures for declarations that include a type annotation AND an init value, such as:
        //       var x::Int32 = 'some string'
        //       var x::Int32 = some_str_function()
    }

    @Test
    public void test98() throws Exception {
        /*
             TODO: Create a test for infinite type exception (occurs check)
         */
    }

    @Test
    public void test99() throws Exception {
        /*
             TODO: Create a test case with a nested begin-end that shadows an
                   an outer var with a different type
         */
    }

}
