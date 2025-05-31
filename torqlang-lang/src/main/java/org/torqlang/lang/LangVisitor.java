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

    R visitAnyType(AnyType lang, T state) throws Exception;

    R visitApplyLang(ApplyLang lang, T state) throws Exception;

    R visitArrayType(ArrayType lang, T state) throws Exception;

    R visitAskStmt(AskStmt lang, T state) throws Exception;

    R visitBeginLang(BeginLang lang, T state) throws Exception;

    R visitBoolAsExpr(BoolAsExpr lang, T state) throws Exception;

    R visitBoolAsPat(BoolAsPat lang, T state) throws Exception;

    R visitBoolAsType(BoolAsType lang, T state) throws Exception;

    R visitBoolType(BoolType lang, T state) throws Exception;

    R visitBreakStmt(BreakStmt lang, T state) throws Exception;

    R visitCaseClause(CaseClause lang, T state) throws Exception;

    R visitCaseLang(CaseLang lang, T state) throws Exception;

    R visitCatchClause(CatchClause lang, T state) throws Exception;

    R visitCharAsExpr(CharAsExpr lang, T state) throws Exception;

    R visitCharAsType(CharAsType lang, T state) throws Exception;

    R visitCharType(CharType lang, T state) throws Exception;

    R visitContinueStmt(ContinueStmt lang, T state) throws Exception;

    R visitDec128AsExpr(Dec128AsExpr lang, T state) throws Exception;

    R visitDec128AsType(Dec128AsType lang, T state) throws Exception;

    R visitDec128Type(Dec128Type lang, T state) throws Exception;

    R visitDotSelectExpr(DotSelectExpr lang, T state) throws Exception;

    R visitEofAsExpr(EofAsExpr lang, T state) throws Exception;

    R visitEofAsPat(EofAsPat lang, T state) throws Exception;

    R visitEofAsType(EofAsType lang, T state) throws Exception;

    R visitEofType(EofType lang, T state) throws Exception;

    R visitFieldExpr(FieldExpr lang, T state) throws Exception;

    R visitFieldPat(FieldPat lang, T state) throws Exception;

    R visitFieldType(FieldType lang, T state) throws Exception;

    R visitFlt32AsType(Flt32AsType lang, T state) throws Exception;

    R visitFlt32Type(Flt32Type lang, T state) throws Exception;

    R visitFlt64AsExpr(Flt64AsExpr lang, T state) throws Exception;

    R visitFlt64AsType(Flt64AsType lang, T state) throws Exception;

    R visitFlt64Type(Flt64Type lang, T state) throws Exception;

    R visitForStmt(ForStmt lang, T state) throws Exception;

    R visitFuncExpr(FuncExpr lang, T state) throws Exception;

    R visitFuncStmt(FuncStmt lang, T state) throws Exception;

    R visitFuncType(FuncType lang, T state) throws Exception;

    R visitGroupExpr(GroupExpr lang, T state) throws Exception;

    R visitIdentAsExpr(IdentAsExpr lang, T state) throws Exception;

    R visitIdentAsPat(IdentAsPat lang, T state) throws Exception;

    R visitIdentAsProtocol(IdentAsProtocol lang, T state) throws Exception;

    R visitIdentAsType(IdentAsType lang, T state) throws Exception;

    R visitIdentVarDecl(IdentVarDecl lang, T state) throws Exception;

    R visitIfClause(IfClause lang, T state) throws Exception;

    R visitIfLang(IfLang lang, T state) throws Exception;

    R visitImportName(ImportName lang, T state) throws Exception;

    R visitImportStmt(ImportStmt lang, T state) throws Exception;

    R visitIndexSelectExpr(IndexSelectExpr lang, T state) throws Exception;

    R visitInitVarDecl(InitVarDecl lang, T state) throws Exception;

    R visitInt32AsType(Int32AsType lang, T state) throws Exception;

    R visitInt32Type(Int32Type lang, T state) throws Exception;

    R visitInt64AsExpr(Int64AsExpr lang, T state) throws Exception;

    R visitInt64AsType(Int64AsType lang, T state) throws Exception;

    R visitInt64Type(Int64Type lang, T state) throws Exception;

    R visitInt64AsPat(Int64AsPat lang, T state) throws Exception;

    R visitIntersectionProtocol(IntersectionProtocol lang, T state) throws Exception;

    R visitIntersectionType(IntersectionType lang, T state) throws Exception;

    R visitLocalLang(LocalLang lang, T state) throws Exception;

    R visitMetaField(MetaField lang, T state) throws Exception;

    R visitMetaRec(MetaRec lang, T state) throws Exception;

    R visitMetaTuple(MetaTuple lang, T state) throws Exception;

    R visitModuleStmt(ModuleStmt lang, T state) throws Exception;

    R visitNewExpr(NewExpr lang, T state) throws Exception;

    R visitNullAsExpr(NullAsExpr lang, T state) throws Exception;

    R visitNullAsPat(NullAsPat lang, T state) throws Exception;

    R visitNullAsType(NullAsType lang, T state) throws Exception;

    R visitNullType(NullType lang, T state) throws Exception;

    R visitObjType(ObjType lang, T state) throws Exception;

    R visitOrExpr(OrExpr lang, T state) throws Exception;

    R visitPackageStmt(PackageStmt lang, T state) throws Exception;

    R visitProcExpr(ProcExpr lang, T state) throws Exception;

    R visitProcStmt(ProcStmt lang, T state) throws Exception;

    R visitProcType(ProcType lang, T state) throws Exception;

    R visitProductExpr(ProductExpr lang, T state) throws Exception;

    R visitProtocolApply(ProtocolApply lang, T state) throws Exception;

    R visitProtocolAskHandler(ProtocolAskHandler lang, T state) throws Exception;

    R visitProtocolStmt(ProtocolStmt lang, T state) throws Exception;

    R visitProtocolStreamHandler(ProtocolStreamHandler lang, T state) throws Exception;

    R visitProtocolStruct(ProtocolStruct lang, T state) throws Exception;

    R visitProtocolTellHandler(ProtocolTellHandler lang, T state) throws Exception;

    R visitRecExpr(RecExpr lang, T state) throws Exception;

    R visitRecPat(RecPat lang, T state) throws Exception;

    R visitRecType(RecType lang, T state) throws Exception;

    R visitRecTypeExpr(RecTypeExpr lang, T state) throws Exception;

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

    R visitStrAsType(StrAsType lang, T state) throws Exception;

    R visitStrType(StrType lang, T state) throws Exception;

    R visitSumExpr(SumExpr lang, T state) throws Exception;

    R visitTellStmt(TellStmt lang, T state) throws Exception;

    R visitThrowLang(ThrowLang lang, T state) throws Exception;

    R visitTokenType(TokenType lang, T state) throws Exception;

    R visitTryLang(TryLang lang, T state) throws Exception;

    R visitTupleExpr(TupleExpr lang, T state) throws Exception;

    R visitTuplePat(TuplePat lang, T state) throws Exception;

    R visitTupleType(TupleType lang, T state) throws Exception;

    R visitTupleTypeExpr(TupleTypeExpr lang, T state) throws Exception;

    R visitTypeApply(TypeApply lang, T state) throws Exception;

    R visitTypeParam(TypeParam lang, T state) throws Exception;

    R visitTypeStmt(TypeStmt lang, T state) throws Exception;

    R visitUnaryExpr(UnaryExpr lang, T state) throws Exception;

    R visitUnifyStmt(UnifyStmt lang, T state) throws Exception;

    R visitUnionType(UnionType lang, T state) throws Exception;

    R visitVarStmt(VarStmt lang, T state) throws Exception;

    R visitWhileStmt(WhileStmt lang, T state) throws Exception;
}
