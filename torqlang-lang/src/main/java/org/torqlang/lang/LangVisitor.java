/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

/*
 * Many specific types are visited using generic methods. All the methods in the first table below are replaced by the
 * methods in the second table:
 *
 *     SPECIFIC METHODS
 *     Type                AsType              AsExpr              AsPat
 *     ------------------  ------------------  ------------------  ------------------
 *     visitAnyType
 *     visitArrayType
 *     visitBoolType      visitBoolAsType      visitBoolAsExpr     visitBoolAsPat
 *     visitCharType      visitCharAsType      visitCharAsExpr
 *     visitDec128Type    visitDec128AsType    visitDec128AsExpr
 *     visitEofType       visitEofAsType       visitEofAsExpr      visitEofAsPat
 *     visitFlt32Type     visitFlt32AsType     [1]
 *     visitFlt64Type     visitFlt64AsType     visitFlt64AsExpr
 *     visitInt32Type     visitInt32AsType     [1]
 *     visitInt64Type     visitInt64AsType     visitInt64AsExpr    visitInt64AsPat
 *     visitNullType      visitNullAsType      visitNullAsExpr     visitNullAsPat
 *     visitStrType       visitStrAsType       visitStrAsExpr      visitStrAsPat
 *     visitTokenType [2]
 *
 *     GENERAL METHODS
 *     Type                AsType              AsExpr              AsPat
 *     ------------------  ------------------  ------------------  ------------------
 *     visitIdentAsType    visitScalarAsType   visitScalarAsExpr   visitFeatureAsPat [3]
 *
 *     [1] 32-bit values are carried by the tool chain as subtypes of 64-bit values until the concrete value
 *         is needed.
 *
 *     [2] Tokens are unforgeable so there are no "as" methods to express its value.
 *
 *     [3] Currently, Int64 is a subtype of Feature in the KLVM, and Int32 is a subtype of Int64 according to the
 *         Liskov Substitution Principle (LSP). In spite of this, our validation process only allows 32-bit integers as
 *         features because most containers, such as arrays, only allow a 32-bit range of values.
 */
public interface LangVisitor<T, R> {

    R visitActExpr(ActExpr lang, T state) throws Exception;

    R visitActorExpr(ActorExpr lang, T state) throws Exception;

    R visitActorStmt(ActorStmt lang, T state) throws Exception;

    R visitAndExpr(AndExpr lang, T state) throws Exception;

    R visitApplyLang(ApplyLang lang, T state) throws Exception;

    R visitAskStmt(AskStmt lang, T state) throws Exception;

    R visitBeginLang(BeginLang lang, T state) throws Exception;

    R visitBreakStmt(BreakStmt lang, T state) throws Exception;

    R visitCaseClause(CaseClause lang, T state) throws Exception;

    R visitCaseLang(CaseLang lang, T state) throws Exception;

    R visitCatchClause(CatchClause lang, T state) throws Exception;

    R visitContinueStmt(ContinueStmt lang, T state) throws Exception;

    R visitDotSelectExpr(DotSelectExpr lang, T state) throws Exception;

    R visitFeatureAsPat(FeatureAsPat lang, T state) throws Exception;

    R visitFieldExpr(FieldExpr lang, T state) throws Exception;

    R visitFieldPat(FieldPat lang, T state) throws Exception;

    R visitFieldType(FieldType lang, T state) throws Exception;

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

    R visitIntersectionProtocol(IntersectionProtocol lang, T state) throws Exception;

    R visitIntersectionType(IntersectionType lang, T state) throws Exception;

    R visitLocalLang(LocalLang lang, T state) throws Exception;

    R visitMetaField(MetaField lang, T state) throws Exception;

    R visitMetaRec(MetaRec lang, T state) throws Exception;

    R visitMetaTuple(MetaTuple lang, T state) throws Exception;

    R visitModuleStmt(ModuleStmt lang, T state) throws Exception;

    R visitNewExpr(NewExpr lang, T state) throws Exception;

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

    R visitScalarAsExpr(ScalarAsExpr lang, T state) throws Exception;

    R visitScalarAsType(ScalarAsType lang, T state) throws Exception;

    R visitSelectAndApplyLang(SelectAndApplyLang lang, T state) throws Exception;

    R visitSeqLang(SeqLang lang, T state) throws Exception;

    R visitSetCellValueStmt(SetCellValueStmt lang, T state) throws Exception;

    R visitSkipStmt(SkipStmt lang, T state) throws Exception;

    R visitSpawnExpr(SpawnExpr lang, T state) throws Exception;

    R visitSumExpr(SumExpr lang, T state) throws Exception;

    R visitTellStmt(TellStmt lang, T state) throws Exception;

    R visitThrowLang(ThrowLang lang, T state) throws Exception;

    R visitTryLang(TryLang lang, T state) throws Exception;

    R visitTupleExpr(TupleExpr lang, T state) throws Exception;

    R visitTuplePat(TuplePat lang, T state) throws Exception;

    R visitTupleType(TupleType lang, T state) throws Exception;

    R visitTupleTypeExpr(TupleTypeExpr lang, T state) throws Exception;

    R visitTypeApply(TypeApply lang, T state) throws Exception;

    R visitTypeDecl(TypeDecl lang, T state) throws Exception;

    R visitTypeParam(TypeParam lang, T state) throws Exception;

    R visitTypeStmt(TypeStmt lang, T state) throws Exception;

    R visitUnaryExpr(UnaryExpr lang, T state) throws Exception;

    R visitUnifyStmt(UnifyStmt lang, T state) throws Exception;

    R visitUnionType(UnionType lang, T state) throws Exception;

    R visitVarStmt(VarStmt lang, T state) throws Exception;

    R visitWhileStmt(WhileStmt lang, T state) throws Exception;
}
