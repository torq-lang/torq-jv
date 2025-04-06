/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidatorIf {

    @Test
    public void test01() throws Exception {
        String source = """
            if true then
                3
            else
                5
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarInfr.INT32, subst.get(alphaVar));
    }

    @Test
    public void test02() throws Exception {
        String source = """
            if true then
                'three'
            else
                5
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 if true then
            0002     'three'
            0003 else
            0004     5
                     ^__ Expected Str but found Int32
            0005 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test03() throws Exception {
        String source = """
            if true then
                3
            else
                'five'
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 if true then
            0002     3
            0003 else
            0004     'five'
                     ^__ Expected Int32 but found Str
            0005 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test04() throws Exception {
        String source = """
            if true then
                3
            elseif false then
                5
            else
                7
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarInfr.INT32, subst.get(alphaVar));
    }

    @Test
    public void test05() throws Exception {
        String source = """
            if true then
                'three'
            elseif false then
                5
            else
                7
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 if true then
            0002     'three'
            0003 elseif false then
            0004     5
                     ^__ Expected Str but found Int32
            0005 else
            0006     7
            0007 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test06() throws Exception {
        String source = """
            if true then
                3
            elseif false then
                'five'
            else
                7
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 if true then
            0002     3
            0003 elseif false then
            0004     'five'
                     ^__ Expected Int32 but found Str
            0005 else
            0006     7
            0007 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test07() {
        String source = """
            if true then
                3
            elseif false then
                5
            else
                'seven'
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 if true then
            0002     3
            0003 elseif false then
            0004     5
            0005 else
            0006     'seven'
                     ^__ Expected Int32 but found Str
            0007 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test08() throws Exception {
        String source = """
            begin
                var x::Str
                if true then
                    // Note that "x = 3" of type "Int32" shadows the previous type declaration "x::Str"
                    var x = 3
                    x
                elseif false then
                    var y = 5
                    y
                else
                    var z = 7
                    z
                end
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarInfr.INT32, subst.get(alphaVar));
    }

}
