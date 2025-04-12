/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public interface LangVisitor<T, R> {

    R visitActExpr(ActExpr lang, T state) throws Exception;

    R visitActorExpr(ActorExpr lang, T state) throws Exception;

    R visitActorStmt(ActorStmt lang, T state) throws Exception;

    R visitAndExpr(AndExpr lang, T state) throws Exception;

    R visitApplyLang(ApplyLang lang, T state) throws Exception;

    R visitAskStmt(AskStmt lang, T state) throws Exception;

    R visitBeginLang(BeginLang lang, T state) throws Exception;

    R visitBoolAsExpr(BoolAsExpr lang, T state) throws Exception;

    R visitBoolAsPat(BoolAsPat lang, T state) throws Exception;

    R visitBreakStmt(BreakStmt lang, T state) throws Exception;

    R visitCaseClause(CaseClause lang, T state) throws Exception;

    R visitCaseLang(CaseLang lang, T state) throws Exception;

    R visitCatchClause(CatchClause lang, T state) throws Exception;

    R visitCharAsExpr(CharAsExpr lang, T state) throws Exception;

    R visitContinueStmt(ContinueStmt lang, T state) throws Exception;

    R visitDec128AsExpr(Dec128AsExpr lang, T state) throws Exception;

    R visitDotSelectExpr(DotSelectExpr lang, T state) throws Exception;

    R visitEofAsExpr(EofAsExpr lang, T state) throws Exception;

    R visitEofAsPat(EofAsPat lang, T state) throws Exception;

    R visitFieldExpr(FieldExpr lang, T state) throws Exception;

    R visitFieldPat(FieldPat lang, T state) throws Exception;

    R visitFieldType(FieldType lang, T state) throws Exception;

    R visitFltAsExpr(FltAsExpr lang, T state) throws Exception;

    R visitForStmt(ForStmt lang, T state) throws Exception;

    R visitFuncExpr(FuncExpr lang, T state) throws Exception;

    R visitFuncStmt(FuncStmt lang, T state) throws Exception;

    R visitGroupExpr(GroupExpr lang, T state) throws Exception;

    R visitIdentAsExpr(IdentAsExpr lang, T state) throws Exception;

    R visitIdentAsPat(IdentAsPat lang, T state) throws Exception;

    R visitIdentVarDecl(IdentVarDecl lang, T state) throws Exception;

    R visitIfClause(IfClause lang, T state) throws Exception;

    R visitIfLang(IfLang lang, T state) throws Exception;

    R visitImportName(ImportName lang, T state) throws Exception;

    R visitImportStmt(ImportStmt lang, T state) throws Exception;

    R visitIndexSelectExpr(IndexSelectExpr lang, T state) throws Exception;

    R visitInitVarDecl(InitVarDecl lang, T state) throws Exception;

    R visitIntAsExpr(IntAsExpr lang, T state) throws Exception;

    R visitIntAsPat(IntAsPat lang, T state) throws Exception;

    R visitIntersectionType(IntersectionType lang, T state) throws Exception;

    R visitLocalLang(LocalLang lang, T state) throws Exception;

    R visitMetaField(MetaField lang, T state) throws Exception;

    R visitMetaRec(MetaRec lang, T state) throws Exception;

    R visitMetaTuple(MetaTuple lang, T state) throws Exception;

    R visitModuleStmt(ModuleStmt lang, T state) throws Exception;

    R visitNewExpr(NewExpr lang, T state) throws Exception;

    R visitNullAsExpr(NullAsExpr lang, T state) throws Exception;

    R visitNullAsPat(NullAsPat lang, T state) throws Exception;

    R visitOrExpr(OrExpr lang, T state) throws Exception;

    R visitPackageStmt(PackageStmt lang, T state) throws Exception;

    R visitProcExpr(ProcExpr lang, T state) throws Exception;

    R visitProcStmt(ProcStmt lang, T state) throws Exception;

    R visitProductExpr(ProductExpr lang, T state) throws Exception;

    R visitRecExpr(RecExpr lang, T state) throws Exception;

    R visitRecPat(RecPat lang, T state) throws Exception;

    R visitRecType(RecType lang, T state) throws Exception;

    R visitRelationalExpr(RelationalExpr lang, T state) throws Exception;

    R visitRespondStmt(RespondStmt lang, T state) throws Exception;

    R visitReturnStmt(ReturnStmt lang, T state) throws Exception;

    R visitSelectAndApplyLang(SelectAndApplyLang lang, T state) throws Exception;

    R visitSeqLang(SeqLang lang, T state) throws Exception;

    R visitSetCellValueStmt(SetCellValueStmt lang, T state) throws Exception;

    R visitSkipStmt(SkipStmt lang, T state) throws Exception;

    R visitSpawnExpr(SpawnExpr lang, T state) throws Exception;

    R visitStrAsExpr(StrAsExpr lang, T state) throws Exception;

    R visitStrAsPat(StrAsPat lang, T state) throws Exception;

    R visitSumExpr(SumExpr lang, T state) throws Exception;

    R visitTellStmt(TellStmt lang, T state) throws Exception;

    R visitThrowLang(ThrowLang lang, T state) throws Exception;

    R visitTryLang(TryLang lang, T state) throws Exception;

    R visitTupleExpr(TupleExpr lang, T state) throws Exception;

    R visitTuplePat(TuplePat lang, T state) throws Exception;

    R visitTupleType(TupleType lang, T state) throws Exception;

    R visitTypeAnno(TypeAnno lang, T state) throws Exception;

    R visitTypeStmt(TypeStmt lang, T state) throws Exception;

    R visitTypeApplyExpr(ApplyType lang, T state) throws Exception;

    R visitUnaryExpr(UnaryExpr lang, T state) throws Exception;

    R visitUnifyStmt(UnifyStmt lang, T state) throws Exception;

    R visitUnionType(UnionType lang, T state) throws Exception;

    R visitVarStmt(VarStmt lang, T state) throws Exception;

    R visitWhileStmt(WhileStmt lang, T state) throws Exception;
}
