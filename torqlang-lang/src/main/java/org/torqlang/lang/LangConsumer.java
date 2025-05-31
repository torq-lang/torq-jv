/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.function.Consumer;

public class LangConsumer implements LangVisitor<Consumer<Lang>, Void> {

    public static void consume(Lang lang, Consumer<Lang> consumer) throws Exception {
        LangConsumer visitor = new LangConsumer();
        lang.accept(visitor, consumer);
    }

    @Override
    public final Void visitActExpr(ActExpr lang, Consumer<Lang> state) throws Exception {
        lang.seq.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitActorExpr(ActorExpr lang, Consumer<Lang> state) throws Exception {
        visitActorLang(lang, state);
        return null;
    }

    public final void visitActorLang(ActorLang lang, Consumer<Lang> state) throws Exception {
        for (Pat p : lang.params) {
            p.accept(this, state);
        }
        visitConditionally(lang.protocol, state);
        for (StmtOrExpr sox : lang.body) {
            sox.accept(this, state);
        }
        state.accept(lang);
    }

    @Override
    public final Void visitActorStmt(ActorStmt lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        visitActorLang(lang, state);
        return null;
    }

    @Override
    public final Void visitAndExpr(AndExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitAnyType(AnyType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitApplyLang(ApplyLang lang, Consumer<Lang> state) throws Exception {
        lang.proc.accept(this, state);
        for (StmtOrExpr sox : lang.args) {
            sox.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitArrayType(ArrayType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitAskStmt(AskStmt lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        visitConditionally(lang.guard, state);
        lang.body.accept(this, state);
        visitConditionally(lang.responseType, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBeginLang(BeginLang lang, Consumer<Lang> state) throws Exception {
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBoolAsExpr(BoolAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBoolAsPat(BoolAsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBoolAsType(BoolAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBoolType(BoolType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitBreakStmt(BreakStmt lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCaseClause(CaseClause lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        visitConditionally(lang.guard, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCaseLang(CaseLang lang, Consumer<Lang> state) throws Exception {
        lang.arg.accept(this, state);
        lang.caseClause.accept(this, state);
        for (CaseClause caseClause : lang.altCaseClauses) {
            caseClause.accept(this, state);
        }
        lang.elseSeq.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCatchClause(CatchClause lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        lang.guard.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCharAsExpr(CharAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCharAsType(CharAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitCharType(CharType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    private void visitConditionally(Lang lang, Consumer<Lang> state) throws Exception {
        if (lang != null) {
            lang.accept(this, state);
        }
    }

    @Override
    public final Void visitContinueStmt(ContinueStmt lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitDec128AsExpr(Dec128AsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitDec128AsType(Dec128AsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitDec128Type(Dec128Type lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitDotSelectExpr(DotSelectExpr lang, Consumer<Lang> state) throws Exception {
        lang.recExpr.accept(this, state);
        lang.featureExpr.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitEofAsExpr(EofAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitEofAsPat(EofAsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitEofAsType(EofAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitEofType(EofType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFieldExpr(FieldExpr lang, Consumer<Lang> state) throws Exception {
        lang.feature.accept(this, state);
        lang.value.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFieldPat(FieldPat lang, Consumer<Lang> state) throws Exception {
        lang.feature.accept(this, state);
        lang.value.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFieldType(FieldType lang, Consumer<Lang> state) throws Exception {
        lang.feature.accept(this, state);
        lang.value.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFlt32AsType(Flt32AsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFlt32Type(Flt32Type lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFlt64AsExpr(Flt64AsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFlt64AsType(Flt64AsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFlt64Type(Flt64Type lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitForStmt(ForStmt lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        lang.iter.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFuncExpr(FuncExpr lang, Consumer<Lang> state) throws Exception {
        for (Pat p : lang.params) {
            p.accept(this, state);
        }
        lang.returnType.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFuncStmt(FuncStmt lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (Pat p : lang.params) {
            p.accept(this, state);
        }
        lang.returnType.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitFuncType(FuncType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitGroupExpr(GroupExpr lang, Consumer<Lang> state) throws Exception {
        lang.expr.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIdentAsExpr(IdentAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIdentAsPat(IdentAsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIdentAsProtocol(IdentAsProtocol lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIdentAsType(IdentAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIdentVarDecl(IdentVarDecl lang, Consumer<Lang> state) throws Exception {
        lang.identAsPat.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIfClause(IfClause lang, Consumer<Lang> state) throws Exception {
        lang.condition.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIfLang(IfLang lang, Consumer<Lang> state) throws Exception {
        lang.ifClause.accept(this, state);
        for (IfClause ifClause : lang.altIfClauses) {
            ifClause.accept(this, state);
        }
        visitConditionally(lang.elseSeq, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitImportName(ImportName lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        if (lang.alias != null) {
            lang.alias.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitImportStmt(ImportStmt lang, Consumer<Lang> state) throws Exception {
        for (IdentAsExpr id : lang.qualifier) {
            id.accept(this, state);
        }
        for (ImportName in : lang.names) {
            in.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIndexSelectExpr(IndexSelectExpr lang, Consumer<Lang> state) throws Exception {
        lang.recExpr.accept(this, state);
        lang.featureExpr.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInitVarDecl(InitVarDecl lang, Consumer<Lang> state) throws Exception {
        lang.varPat.accept(this, state);
        lang.valueExpr.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt32AsType(Int32AsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt32Type(Int32Type lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt64AsExpr(Int64AsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt64AsType(Int64AsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt64Type(Int64Type lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitInt64AsPat(Int64AsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIntersectionProtocol(IntersectionProtocol lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitIntersectionType(IntersectionType lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitLocalLang(LocalLang lang, Consumer<Lang> state) throws Exception {
        for (VarDecl v : lang.varDecls) {
            v.accept(this, state);
        }
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitMetaField(MetaField lang, Consumer<Lang> state) throws Exception {
        lang.feature.accept(this, state);
        lang.value.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitMetaRec(MetaRec lang, Consumer<Lang> state) throws Exception {
        for (MetaField f : lang.fields()) {
            f.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitMetaTuple(MetaTuple lang, Consumer<Lang> state) throws Exception {
        for (MetaValue v : lang.values()) {
            v.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitModuleStmt(ModuleStmt lang, Consumer<Lang> state) throws Exception {
        lang.packageStmt.accept(this, state);
        for (StmtOrExpr sox : lang.body) {
            sox.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitNewExpr(NewExpr lang, Consumer<Lang> state) throws Exception {
        lang.typeApply.accept(this, state);
        for (StmtOrExpr arg : lang.args) {
            arg.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitNullAsExpr(NullAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitNullAsPat(NullAsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitNullAsType(NullAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitNullType(NullType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitObjType(ObjType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitOrExpr(OrExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitPackageStmt(PackageStmt lang, Consumer<Lang> state) throws Exception {
        for (IdentAsExpr id : lang.path) {
            id.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProcExpr(ProcExpr lang, Consumer<Lang> state) throws Exception {
        for (Pat p : lang.params) {
            p.accept(this, state);
        }
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProcStmt(ProcStmt lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (Pat p : lang.params) {
            p.accept(this, state);
        }
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProcType(ProcType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProductExpr(ProductExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolApply(ProtocolApply lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (Type arg : lang.typeArgs) {
            arg.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolAskHandler(ProtocolAskHandler lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        lang.responseType.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolStmt(ProtocolStmt lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (TypeParam p : lang.typeParams) {
            p.accept(this, state);
        }
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolStreamHandler(ProtocolStreamHandler lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        lang.responseType.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolStruct(ProtocolStruct lang, Consumer<Lang> state) throws Exception {
        for (ProtocolHandler h : lang.handlers) {
            h.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitProtocolTellHandler(ProtocolTellHandler lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRecExpr(RecExpr lang, Consumer<Lang> state) throws Exception {
        visitConditionally(lang.label(), state);
        for (FieldExpr f : lang.fields()) {
            f.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRecPat(RecPat lang, Consumer<Lang> state) throws Exception {
        lang.label().accept(this, state);
        for (FieldPat f : lang.fields()) {
            f.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRecType(RecType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRecTypeExpr(RecTypeExpr lang, Consumer<Lang> state) throws Exception {
        visitConditionally(lang.label, state);
        for (FieldType f : lang.fields) {
            f.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRelationalExpr(RelationalExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitRespondStmt(RespondStmt lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitReturnStmt(ReturnStmt lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSelectAndApplyLang(SelectAndApplyLang lang, Consumer<Lang> state) throws Exception {
        lang.selectExpr.accept(this, state);
        for (StmtOrExpr sox : lang.args) {
            sox.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSeqLang(SeqLang lang, Consumer<Lang> state) throws Exception {
        for (StmtOrExpr sox : lang.list) {
            sox.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSetCellValueStmt(SetCellValueStmt lang, Consumer<Lang> state) throws Exception {
        lang.leftSide.accept(this, state);
        lang.rightSide.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSkipStmt(SkipStmt lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSpawnExpr(SpawnExpr lang, Consumer<Lang> state) throws Exception {
        for (StmtOrExpr sox : lang.args) {
            sox.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitStrAsExpr(StrAsExpr lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitStrAsPat(StrAsPat lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitStrAsType(StrAsType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitStrType(StrType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitSumExpr(SumExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTellStmt(TellStmt lang, Consumer<Lang> state) throws Exception {
        lang.pat.accept(this, state);
        lang.guard.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitThrowLang(ThrowLang lang, Consumer<Lang> state) throws Exception {
        lang.arg.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTokenType(TokenType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTryLang(TryLang lang, Consumer<Lang> state) throws Exception {
        lang.body.accept(this, state);
        for (CatchClause c : lang.catchClauses) {
            c.accept(this, state);
        }
        lang.finallySeq.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTupleExpr(TupleExpr lang, Consumer<Lang> state) throws Exception {
        visitConditionally(lang.label(), state);
        for (StmtOrExpr v : lang.values()) {
            v.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTuplePat(TuplePat lang, Consumer<Lang> state) throws Exception {
        visitConditionally(lang.label(), state);
        for (Pat v : lang.values()) {
            v.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTupleType(TupleType lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTupleTypeExpr(TupleTypeExpr lang, Consumer<Lang> state) throws Exception {
        lang.label.accept(this, state);
        for (Type v : lang.values) {
            v.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTypeApply(TypeApply lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (Type arg : lang.typeArgs) {
            arg.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTypeParam(TypeParam lang, Consumer<Lang> state) {
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitTypeStmt(TypeStmt lang, Consumer<Lang> state) throws Exception {
        lang.name.accept(this, state);
        for (TypeParam p : lang.typeParams) {
            p.accept(this, state);
        }
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitUnaryExpr(UnaryExpr lang, Consumer<Lang> state) throws Exception {
        lang.arg.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitUnifyStmt(UnifyStmt lang, Consumer<Lang> state) throws Exception {
        lang.leftSide.accept(this, state);
        lang.rightSide.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitUnionType(UnionType lang, Consumer<Lang> state) throws Exception {
        lang.arg1.accept(this, state);
        lang.arg2.accept(this, state);
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitVarStmt(VarStmt lang, Consumer<Lang> state) throws Exception {
        for (VarDecl v : lang.varDecls) {
            v.accept(this, state);
        }
        state.accept(lang);
        return null;
    }

    @Override
    public final Void visitWhileStmt(WhileStmt lang, Consumer<Lang> state) throws Exception {
        lang.cond.accept(this, state);
        lang.body.accept(this, state);
        state.accept(lang);
        return null;
    }

}
