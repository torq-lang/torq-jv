/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.Ident;
import org.torqlang.klvm.IdentDef;
import org.torqlang.klvm.Instr;

/*
 * When entering a new method (function or procedure) body:
 *
 * -- We allocate a new `JumpFlags` with `return` as an allowed jump operation
 * -- The same `return` flag is shared as we enter loop bodies
 *
 * When entering a new loop body:
 *
 * -- Allocate a `JumpFlags` with `break` and `continue` allowed, but carry forward the shared `return` flag.
 */
public final class LocalTarget {

    public static final int ALLOWED = -1;
    public static final int NOT_ALLOWED = 0;
    public static final int USED = 1;

    private final LocalTargetType type;
    private final JumpFlags jumpFlags;
    private final LexicalScope scope;

    private Ident offeredIdent;

    private LocalTarget(LocalTargetType type, Ident offeredIdent, JumpFlags jumpFlags, LexicalScope scope) {
        this.type = type;
        this.offeredIdent = offeredIdent;
        this.jumpFlags = jumpFlags;
        this.scope = scope;
    }

    public static LocalTarget createExprTargetForFuncBody(Ident offeredIdent) {
        return new LocalTarget(LocalTargetType.EXPR, offeredIdent, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(ALLOWED)), new LexicalScope());
    }

    public static LocalTarget createExprTargetForRoot(Ident exprIdent) {
        return new LocalTarget(LocalTargetType.EXPR, exprIdent, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(NOT_ALLOWED)), new LexicalScope());
    }

    public static LocalTarget createStmtTargetForFinally() {
        return new LocalTarget(LocalTargetType.STMT, null, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(NOT_ALLOWED)), new LexicalScope());
    }

    public static LocalTarget createStmtTargetForProcBody() {
        return new LocalTarget(LocalTargetType.STMT, null, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(ALLOWED)), new LexicalScope());
    }

    public static LocalTarget createStmtTargetForRoot() {
        return new LocalTarget(LocalTargetType.STMT, null, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(NOT_ALLOWED)), new LexicalScope());
    }

    public void acceptOfferedIdent() {
        this.offeredIdent = null;
    }

    public final void addIdentDef(IdentDef identDef) {
        scope.addIdentDef(identDef);
    }

    public final void addInstr(Instr instr) {
        scope.addInstr(instr);
    }

    public final LocalTarget asAskTargetWithNewScope(Ident exprIdent) {
        if (exprIdent == null) {
            throw new NullPointerException("exprIdent");
        }
        return new LocalTarget(LocalTargetType.EXPR, exprIdent, new JumpFlags(NOT_ALLOWED, NOT_ALLOWED,
            new SharedFlag(ALLOWED)), new LexicalScope());
    }

    /*
     * This method is used by binary and unary expressions to create intermediate results that bind to new synthetic
     * identifiers. Subsequently, the intermediate identifiers are used as arguments in binary or unary expressions.
     */
    public LocalTarget asExprTargetWithNewScope() {
        return new LocalTarget(LocalTargetType.EXPR, null, jumpFlags, new LexicalScope());
    }

    /*
     * This method is used by Lang statements that may be an Expr or Stmt:
     */
    public final LocalTarget asExprTargetWithNewScope(Ident exprIdent) {
        if (exprIdent == null) {
            throw new NullPointerException("exprIdent");
        }
        return new LocalTarget(LocalTargetType.EXPR, exprIdent, jumpFlags, new LexicalScope());
    }

    public LocalTarget asExprTargetWithSameScope() {
        return new LocalTarget(LocalTargetType.EXPR, null, jumpFlags, scope);
    }

    /*
     * This method is used by statements to bind intermediate expressions to special identifiers:
     */
    public final LocalTarget asExprTargetWithSameScope(Ident exprIdent) {
        if (exprIdent == null) {
            throw new NullPointerException("exprIdent");
        }
        return new LocalTarget(LocalTargetType.EXPR, exprIdent, jumpFlags, scope);
    }

    /*
     * This method is used by statements to structure loop bodies with jump flags:
     */
    public final LocalTarget asStmtTargetForLoopBodyWithNewScope() {
        return new LocalTarget(LocalTargetType.STMT, null,
            new JumpFlags(ALLOWED, ALLOWED, jumpFlags.returnFlag), new LexicalScope());
    }

    /*
     * This method is used by statements to create new lexical scopes:
     */
    public final LocalTarget asStmtTargetWithNewScope() {
        return new LocalTarget(LocalTargetType.STMT, null, jumpFlags, new LexicalScope());
    }

    /*
     * This method is used by `visitBodyList` while visiting all but the last entry of the list.
     */
    public final LocalTarget asStmtTargetWithSameScope() {
        return new LocalTarget(LocalTargetType.STMT, null, jumpFlags, scope);
    }

    public final int breakFlag() {
        return jumpFlags.breakFlag;
    }

    public final Instr build() {
        return scope.build();
    }

    public final int continueFlag() {
        return jumpFlags.continueFlag;
    }

    public final boolean isBreakAllowed() {
        return jumpFlags.breakFlag == ALLOWED || jumpFlags.breakFlag == USED;
    }

    public final boolean isBreakUsed() {
        return jumpFlags.breakFlag == USED;
    }

    public final boolean isContinueAllowed() {
        return jumpFlags.continueFlag == ALLOWED || jumpFlags.continueFlag == USED;
    }

    public final boolean isContinueUsed() {
        return jumpFlags.continueFlag == USED;
    }

    public final boolean isExprTarget() {
        return type == LocalTargetType.EXPR;
    }

    public final boolean isReturnAllowed() {
        return jumpFlags.returnFlag.value == ALLOWED || jumpFlags.returnFlag.value == USED;
    }

    public final boolean isReturnUsed() {
        return jumpFlags.returnFlag.value == USED;
    }

    public final boolean isStmtTarget() {
        return type == LocalTargetType.STMT;
    }

    public final Ident offeredIdent() {
        return offeredIdent;
    }

    public final int returnFlag() {
        return jumpFlags.returnFlag.value;
    }

    public final LexicalScope scope() {
        return scope;
    }

    public final void setBreakUsed() {
        jumpFlags.breakFlag = Math.abs(jumpFlags.breakFlag);
    }

    public final void setContinueUsed() {
        jumpFlags.continueFlag = Math.abs(jumpFlags.continueFlag);
    }

    public final void setReturnUsed() {
        jumpFlags.returnFlag.value = Math.abs(jumpFlags.returnFlag.value);
    }

    private static enum LocalTargetType {
        EXPR,
        STMT
    }

    /*
     *  0 = Operation not allowed
     * -1 = Operation is allowed but not used
     *  1 = Operation is allowed and used
     */
    private static class JumpFlags {
        private final SharedFlag returnFlag;
        private int breakFlag;
        private int continueFlag;

        private JumpFlags(int breakFlag, int continueFlag, SharedFlag returnFlag) {
            this.breakFlag = breakFlag;
            this.continueFlag = continueFlag;
            this.returnFlag = returnFlag;
        }
    }

    private static class SharedFlag {
        private int value;

        private SharedFlag(int value) {
            this.value = value;
        }
    }

}
