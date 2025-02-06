/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidatorBegin {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                3 + 5
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
    public void test02() throws Exception {
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

    @Test
    public void test03() throws Exception {
        String source = """
            begin
                var x
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

    @Test
    public void test04() throws Exception {
        String source = """
            begin
                var x = 3
                var y
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
    public void test05() throws Exception {
        String source = """
            begin
                var y
                3 + y
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
    public void test06() throws Exception {
        String source = """
            begin
                var x
                x + 5
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
    public void test07() throws Exception {
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
    public void test08() throws Exception {
        String source = """
            begin
                var x
                var y
                x + y
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertInstanceOf(VarType.class, subst.get(alphaVar));
        VarType betaVarResult = (VarType) subst.get(alphaVar);
        assertTrue(betaVarResult.name().startsWith(PolyType.LOWER_BETA));
    }

    @Test
    public void test09() {
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
    public void test10() {
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
    public void test11() throws Exception {
        String source = """
            begin
                begin var x = 3; x end + begin var x = 5; x end
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
    public void test12() throws Exception {
        String source = """
            begin
                var x = 'covered_string' // This x will not be seen and its type will have no affect
                begin var x = 3; x end + begin var x = 5; x end
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
    public void test13() throws Exception {
        String source = """
            begin
                var x, y, z
                x = 1
                y = 'two'
                z = y
                z
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarType alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarType.STR, subst.get(alphaVar));
    }

    @Test
    public void test97() {
        // TODO: Add test failures for declarations that include a type annotation AND an init value, such as:
        //       var x::Int32 = 'some string'
        //       var x::Int32 = some_str_function()
    }

    @Test
    public void test98() {
        /*
             TODO: Create a test for infinite type exception (occurs check)
         */
    }

}
