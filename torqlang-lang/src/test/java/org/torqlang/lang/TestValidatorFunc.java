/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidatorFunc {

    @Test
    public void test01() throws Exception {
        String source = """
            begin
                func id(a) in
                    a
                end
                id(3)
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
            begin
                func id(a) in
                    a
                end
                id('three')
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeSubst subst = sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar));
        assertEquals(ScalarInfr.STR, subst.get(alphaVar));
    }

    @Test
    public void test03() throws Exception {
        String source = """
            begin
                func sum_args(a::Int32, b::Int32) -> Int32 in
                    a + b
                end
                sum_args(3, 5)
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
    public void test04() throws Exception {
        String source = """
            begin
                func sum_args(a::Int32, b::Int32) in
                    a + b
                end
                sum_args(3, 5)
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
            begin
                func sum_args(a::Int32, b) in
                    a + b
                end
                sum_args(3, 5)
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
    public void test06() throws Exception {
        String source = """
            begin
                func sum_args(a, b::Int32) in
                    a + b
                end
                sum_args(3, 5)
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
    public void test07() throws Exception {
        String source = """
            begin
                func sum_args(a, b) -> Int32 in
                    a + b
                end
                sum_args(3, 5)
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
    public void test08() throws Exception {
        String source = """
            begin
                func sum_args(a, b) in
                    a + b
                end
                sum_args(3, 5)
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
    public void test09() {
        String source = """
            begin
                func sum_args(a, a) in
                    a + b
                end
                sum_args(3, 5)
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        AlreadyDefinedInScopeError error = assertThrows(AlreadyDefinedInScopeError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 begin
            0002     func sum_args(a, a) in
                                      ^__ Already defined in scope
            0003         a + b
            0004     end
            0005     sum_args(3, 5)
            0006 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test10() {
        String source = """
            begin
                func sum_args(a::Int32, b::Int32) -> Int64 in
                    a + b
                end
                sum_args(3, 5)
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 begin
            0002     func sum_args(a::Int32, b::Int32) -> Int64 in
            0003         a + b
                             ^__ Expected Int64 but found Int32
            0004     end
            0005     sum_args(3, 5)
            0006 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

    @Test
    public void test11() {
        String source = """
            begin
                func sum_args(a::Int32, b::Int32) -> Int32 in
                    a + b
                end
                sum_args(3, 'five')
            end""";
        Parser p = new Parser(source);
        StmtOrExpr sox = p.parse();
        SuffixFactory suffixFactory = new SuffixFactory();
        Validator validator = new Validator(suffixFactory);
        VarInfr alphaVar = suffixFactory.nextAlphaVar();
        TypeConflictError error = assertThrows(TypeConflictError.class,
            () -> sox.accept(validator, new TypeScope(TypeEnv.create(), alphaVar)));
        String expectedErrorString = """
            0001 begin
            0002     func sum_args(a::Int32, b::Int32) -> Int32 in
            0003         a + b
            0004     end
            0005     sum_args(3, 'five')
                                 ^__ Expected Int32 but found Str
            0006 end""";
        String errorString = error.formatWithSource(4, 5, 5);
        assertEquals(expectedErrorString, errorString);
    }

}
