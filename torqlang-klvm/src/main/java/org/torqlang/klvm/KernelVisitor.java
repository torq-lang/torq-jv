/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public interface KernelVisitor<T, R> {

    R visitActInstr(ActInstr kernel, T state) throws Exception;

    R visitActorCfg(ActorCfg kernel, T state) throws Exception;

    R visitActorCtor(ActorCtor kernel, T state) throws Exception;

    R visitAddInstr(AddInstr kernel, T state) throws Exception;

    R visitApplyInstr(ApplyInstr kernel, T state) throws Exception;

    R visitBindCompleteToCompleteInstr(BindCompleteToCompleteInstr kernel, T state) throws Exception;

    R visitBindCompleteToIdentInstr(BindCompleteToIdentInstr kernel, T state) throws Exception;

    R visitBindCompleteToValueOrVarInstr(BindCompleteToValueOrVarInstr kernel, T state) throws Exception;

    R visitBindIdentToIdentInstr(BindIdentToIdentInstr kernel, T state) throws Exception;

    R visitCaseElseInstr(CaseElseInstr kernel, T state) throws Exception;

    R visitCaseInstr(CaseInstr kernel, T state) throws Exception;

    R visitCatchInstr(CatchInstr kernel, T state) throws Exception;

    R visitClosure(Closure kernel, T state) throws Exception;

    R visitCreateActorCtorInstr(CreateActorCtorInstr kernel, T state) throws Exception;

    R visitCreateProcInstr(CreateProcInstr kernel, T state) throws Exception;

    R visitCreateRecInstr(CreateRecInstr kernel, T state) throws Exception;

    R visitCreateTupleInstr(CreateTupleInstr kernel, T state) throws Exception;

    R visitDebugInstr(DebugInstr kernel, T state) throws Exception;

    R visitDisentailsInstr(DisentailsInstr kernel, T state) throws Exception;

    R visitDivideInstr(DivideInstr kernel, T state) throws Exception;

    R visitEntailsInstr(EntailsInstr kernel, T state) throws Exception;

    R visitEnv(Env kernel, T state) throws Exception;

    R visitFailedValue(FailedValue kernel, T state) throws Exception;

    R visitFieldDef(FieldDef kernel, T state) throws Exception;

    R visitFieldPtn(FieldPtn kernel, T state) throws Exception;

    R visitGetCellValueInstr(GetCellValueInstr kernel, T state) throws Exception;

    R visitGreaterThanOrEqualToInstr(GreaterThanOrEqualToInstr kernel, T state) throws Exception;

    R visitGreaterThanInstr(GreaterThanInstr kernel, T state) throws Exception;

    R visitIdent(Ident kernel, T state) throws Exception;

    R visitIdentDef(IdentDef kernel, T state) throws Exception;

    R visitIdentPtn(IdentPtn kernel, T state) throws Exception;

    R visitIfElseInstr(IfElseInstr kernel, T state) throws Exception;

    R visitIfInstr(IfInstr kernel, T state) throws Exception;

    R visitJumpCatchInstr(JumpCatchInstr kernel, T state) throws Exception;

    R visitJumpThrowInstr(JumpThrowInstr kernel, T state) throws Exception;

    R visitLessThanOrEqualToInstr(LessThanOrEqualToInstr kernel, T state) throws Exception;

    R visitLessThanInstr(LessThanInstr kernel, T state) throws Exception;

    R visitLocalInstr(LocalInstr kernel, T state) throws Exception;

    R visitModuloInstr(ModuloInstr kernel, T state) throws Exception;

    R visitMultiplyInstr(MultiplyInstr kernel, T state) throws Exception;

    R visitNegateInstr(NegateInstr kernel, T state) throws Exception;

    R visitNotInstr(NotInstr kernel, T state) throws Exception;

    R visitObj(Obj kernel, T state) throws Exception;

    R visitOpaqueValue(OpaqueValue kernel, T state) throws Exception;

    R visitProc(Proc kernel, T state) throws Exception;

    R visitProcDef(ProcDef kernel, T state) throws Exception;

    R visitRec(Rec kernel, T state) throws Exception;

    R visitRecDef(RecDef kernel, T state) throws Exception;

    R visitRecPtn(RecPtn kernel, T state) throws Exception;

    R visitResolvedFieldPtn(ResolvedFieldPtn kernel, T state) throws Exception;

    R visitResolvedIdentPtn(ResolvedIdentPtn kernel, T state) throws Exception;

    R visitResolvedRecPtn(ResolvedRecPtn kernel, T state) throws Exception;

    R visitScalar(Scalar kernel, T state) throws Exception;

    R visitSelectAndApplyInstr(SelectAndApplyInstr kernel, T state) throws Exception;

    R visitSelectInstr(SelectInstr kernel, T state) throws Exception;

    R visitSeqInstr(SeqInstr kernel, T state) throws Exception;

    R visitSetCellValueInstr(SetCellValueInstr kernel, T state) throws Exception;

    R visitSkipInstr(SkipInstr kernel, T state) throws Exception;

    R visitStack(Stack kernel, T state) throws Exception;

    R visitSubtractInstr(SubtractInstr kernel, T state) throws Exception;

    R visitThrowInstr(ThrowInstr kernel, T state) throws Exception;

    R visitTryInstr(TryInstr kernel, T state) throws Exception;

    R visitTupleDef(TupleDef kernel, T state) throws Exception;

    R visitValueDef(ValueDef kernel, T state) throws Exception;

    R visitVar(Var kernel, T state) throws Exception;

    R visitVarSet(VarSet kernel, T state) throws Exception;
}
