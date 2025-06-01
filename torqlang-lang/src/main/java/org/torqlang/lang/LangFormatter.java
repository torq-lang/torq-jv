/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.FormatterState;
import org.torqlang.util.NeedsImpl;

import java.io.StringWriter;
import java.util.List;

public final class LangFormatter implements LangVisitor<FormatterState, Void> {

    public static final LangFormatter DEFAULT = new LangFormatter();

    public final String format(Lang lang) {
        try (StringWriter sw = new StringWriter()) {
            FormatterState state = new FormatterState(sw);
            lang.accept(this, state);
            state.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void formatBinaryExpr(String oper, StmtOrExpr arg1, StmtOrExpr arg2, FormatterState state) throws Exception {
        arg1.accept(this, state.inline());
        state.write(FormatterState.SPACE);
        state.write(oper);
        state.write(FormatterState.SPACE);
        arg2.accept(this, state.inline());
    }

    private boolean formatAsMethodType(FieldType fieldType) {
        if (!(fieldType.value instanceof MethodType methodType)) {
            return false;
        }
        if (methodType.name() == null) {
            return false;
        }
        if (!(fieldType.feature instanceof StrAsType featureStrAsType)) {
            return false;
        }
        String methodName = methodType.name().ident.name;
        String featureName = featureStrAsType.str.value;
        return methodName.equals(featureName);
    }

    private void maybeWriteMeta(Lang lang, FormatterState state) throws Exception {
        if (lang.metaStruct() != null) {
            lang.metaStruct().accept(this, state.inline());
            state.writeNewLineAndIndent();
        }
    }

    @Override
    public final Void visitActExpr(ActExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("act");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.seq.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitActorExpr(ActorExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("actor ");
        visitActorLang(lang, state);
        return null;
    }

    private void visitActorLang(ActorLang lang, FormatterState state) throws Exception {
        state.write('(');
        visitParams(lang.params, state.inline());
        state.write(')');
        if (lang.protocol != null) {
            state.write(" implements ");
            lang.protocol.accept(this, state.inline());
            if (lang.protocolName != null) {
                state.write(" as ");
                lang.protocolName.accept(this, state.inline());
            }
        }
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        visitSeqList(lang.body, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
    }

    @Override
    public final Void visitActorStmt(ActorStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("actor ");
        lang.name.accept(this, state.inline());
        visitActorLang(lang, state);
        return null;
    }

    private void visitArgs(List<StmtOrExpr> args, FormatterState state) throws Exception {
        for (int i = 0; i < args.size(); i++) {
            StmtOrExpr next = args.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
    }

    @Override
    public final Void visitAndExpr(AndExpr lang, FormatterState state) throws Exception {
        formatBinaryExpr(SymbolsAndKeywords.AND_OPER, lang.arg1, lang.arg2, state);
        return null;
    }

    @Override
    public final Void visitApplyLang(ApplyLang lang, FormatterState state) throws Exception {
        lang.proc.accept(this, state.inline());
        state.write('(');
        visitArgs(lang.args, state);
        state.write(')');
        return null;
    }

    @Override
    public final Void visitAskStmt(AskStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("handle ask ");
        lang.pat.accept(this, state.inline());
        if (lang.responseType != null) {
            state.write(" -> ");
            lang.responseType.accept(this, state.inline());
        }
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitBeginLang(BeginLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("begin");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitBreakStmt(BreakStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("break");
        return null;
    }

    @Override
    public final Void visitCaseClause(CaseClause lang, FormatterState state) throws Exception {
        state.write("of ");
        lang.pat.accept(this, state.inline());
        if (lang.guard != null) {
            state.write(" when ");
            lang.guard.accept(this, state.inline());
        }
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        return null;
    }

    @Override
    public final Void visitCaseLang(CaseLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("case ");
        lang.arg.accept(this, state.inline());
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.caseClause.accept(this, nextLevelState);
        for (CaseClause altCaseClause : lang.altCaseClauses) {
            nextLevelState.writeNewLineAndIndent();
            altCaseClause.accept(this, nextLevelState);
        }
        if (lang.elseSeq != null) {
            nextLevelState.writeAfterNewLineAndIdent("else");
            FormatterState thirdLevelState = nextLevelState.nextLevel();
            thirdLevelState.writeNewLineAndIndent();
            lang.elseSeq.accept(this, thirdLevelState);
        }
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitCatchClause(CatchClause lang, FormatterState state) throws Exception {
        state.write("catch ");
        lang.pat.accept(this, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        return null;
    }

    @Override
    public final Void visitContinueStmt(ContinueStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("continue");
        return null;
    }

    @Override
    public final Void visitDotSelectExpr(DotSelectExpr lang, FormatterState state) throws Exception {
        lang.recExpr.accept(this, state.inline());
        state.write('.');
        lang.featureExpr.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitFeatureAsPat(FeatureAsPat lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        FormatAsKernelString formattable = (FormatAsKernelString) lang.value();
        state.write(formattable.formatAsKernelString());
        return null;
    }

    @Override
    public final Void visitFieldExpr(FieldExpr lang, FormatterState state) throws Exception {
        lang.feature.accept(this, state);
        state.write(": ");
        lang.value.accept(this, state);
        return null;
    }

    @Override
    public final Void visitFieldPat(FieldPat lang, FormatterState state) throws Exception {
        lang.feature.accept(this, state);
        state.write(": ");
        lang.value.accept(this, state);
        return null;
    }

    @Override
    public final Void visitFieldType(FieldType lang, FormatterState state) throws Exception {
        if (formatAsMethodType(lang)) {
            lang.value.accept(this, state);
        } else {
            lang.feature.accept(this, state);
            state.write(": ");
            lang.value.accept(this, state);
        }
        return null;
    }

    @Override
    public final Void visitForStmt(ForStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("for ");
        lang.pat.accept(this, state.inline());
        state.write(" in ");
        lang.iter.accept(this, state.inline());
        state.write(" do");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    private void visitParams(List<Pat> params, FormatterState state) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            Pat next = params.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
    }

    @Override
    public final Void visitFuncExpr(FuncExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("func ");
        visitFuncLang(lang, state);
        return null;
    }

    private void visitFuncLang(FuncLang lang, FormatterState state) throws Exception {
        state.write('(');
        visitParams(lang.params, state.inline());
        state.write(')');
        if (lang.returnType != null) {
            state.write(" -> ");
            lang.returnType.accept(this, state.inline());
        }
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        visitSeqList(lang.body.list, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
    }

    @Override
    public final Void visitFuncStmt(FuncStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("func ");
        lang.name.accept(this, state.inline());
        visitFuncLang(lang, state);
        return null;
    }

    @Override
    public final Void visitFuncType(FuncType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("func ");
        if (lang.name != null) {
            lang.name.accept(this, state.inline());
        }
        state.write('(');
        visitParams(lang.params, state.inline());
        state.write(')');
        state.write(" -> ");
        lang.returnType.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitGroupExpr(GroupExpr lang, FormatterState state) throws Exception {
        state.write('(');
        lang.expr.accept(this, state);
        state.write(')');
        return null;
    }

    private void visitIdent(Ident ident, FormatterState state) throws Exception {
        if (Ident.isSimpleName(ident.name)) {
            state.write(ident.name);
        } else {
            state.write(Ident.quote(ident.name));
        }
    }

    @Override
    public final Void visitIdentAsExpr(IdentAsExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        visitIdent(lang.ident, state);
        return null;
    }

    @Override
    public final Void visitIdentAsPat(IdentAsPat lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        if (lang.escaped) {
            state.write('~');
        }
        visitIdent(lang.ident, state);
        if (lang.type != null) {
            state.write(SymbolsAndKeywords.TYPE_OPER);
            lang.type.accept(this, state.inline());
        }
        return null;
    }

    @Override
    public final Void visitIdentAsProtocol(IdentAsProtocol lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        visitIdent(lang.protocolIdent(), state);
        return null;
    }

    @Override
    public final Void visitIdentAsType(IdentAsType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        visitIdent(lang.ident(), state);
        return null;
    }

    @Override
    public final Void visitIdentVarDecl(IdentVarDecl lang, FormatterState state) throws Exception {
        lang.identAsPat.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitIfClause(IfClause lang, FormatterState state) throws Exception {
        lang.condition.accept(this, state.inline());
        state.write(" then");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        return null;
    }

    @Override
    public final Void visitIfLang(IfLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("if ");
        lang.ifClause.accept(this, state);
        for (IfClause altIfClause : lang.altIfClauses) {
            state.writeAfterNewLineAndIdent("elseif ");
            altIfClause.accept(this, state);
        }
        if (lang.elseSeq != null) {
            state.writeAfterNewLineAndIdent("else");
            FormatterState nextLevelState = state.nextLevel();
            nextLevelState.writeNewLineAndIndent();
            lang.elseSeq.accept(this, nextLevelState);
        }
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitImportName(ImportName lang, FormatterState state) throws Exception {
        lang.name.accept(this, state.inline());
        if (lang.alias != null) {
            state.write(" as ");
            lang.alias.accept(this, state.inline());
        }
        return null;
    }

    @Override
    public final Void visitImportStmt(ImportStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        List<IdentAsExpr> q = lang.qualifier;
        List<ImportName> ins = lang.names;
        state.write("import ");
        for (int i = 0; i < q.size(); i++) {
            if (i > 0) {
                state.write(".");
            }
            q.get(i).accept(this, state.inline());
        }
        if (ins.size() == 1 && ins.get(0).alias == null) {
            state.write('.');
            ins.get(0).accept(this, state.inline());
        } else {
            state.write(".{");
            for (int i = 0; i < ins.size(); i++) {
                if (i > 0) {
                    state.write(", ");
                }
                ins.get(i).accept(this, state.inline());
            }
            state.write('}');
        }
        return null;
    }

    @Override
    public final Void visitIndexSelectExpr(IndexSelectExpr lang, FormatterState state) throws Exception {
        lang.recExpr.accept(this, state.inline());
        state.write('[');
        lang.featureExpr.accept(this, state.inline());
        state.write(']');
        return null;
    }

    @Override
    public final Void visitInitVarDecl(InitVarDecl lang, FormatterState state) throws Exception {
        lang.varPat.accept(this, state.inline());
        state.write(" = ");
        lang.valueExpr.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitIntersectionProtocol(IntersectionProtocol lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        lang.arg1.accept(this, state.inline());
        state.write(" & ");
        lang.arg2.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitIntersectionType(IntersectionType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        lang.arg1.accept(this, state.inline());
        state.write(" & ");
        lang.arg2.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitLocalLang(LocalLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("local ");
        visitVarDecls(lang.varDecls, state);
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitMetaField(MetaField lang, FormatterState state) throws Exception {
        lang.feature.accept(this, state);
        state.write(": ");
        visitMetaValue(lang.value, state);
        return null;
    }

    @Override
    public final Void visitMetaRec(MetaRec lang, FormatterState state) throws Exception {
        visitMetaRec(lang, state, true);
        return null;
    }

    private void visitMetaRec(MetaRec lang, FormatterState state, boolean showLabel) throws Exception {
        if (showLabel) {
            state.write("meta#");
        }
        state.write('{');
        List<MetaField> fields = lang.fields();
        for (int i = 0; i < fields.size(); i++) {
            MetaField next = fields.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write('}');
    }

    @Override
    public final Void visitMetaTuple(MetaTuple lang, FormatterState state) throws Exception {
        visitMetaTuple(lang, state, true);
        return null;
    }

    private void visitMetaTuple(MetaTuple lang, FormatterState state, boolean showLabel) throws Exception {
        if (showLabel) {
            state.write("meta#");
        }
        state.write('[');
        List<MetaValue> values = lang.values();
        for (int i = 0; i < values.size(); i++) {
            MetaValue next = values.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write(']');
    }

    private void visitMetaValue(MetaValue lang, FormatterState state) throws Exception {
        if (lang instanceof MetaRec) {
            visitMetaRec((MetaRec) lang, state, false);
        } else if (lang instanceof MetaTuple) {
            visitMetaTuple((MetaTuple) lang, state, false);
        } else {
            lang.accept(this, state);
        }
    }

    @Override
    public final Void visitModuleStmt(ModuleStmt lang, FormatterState state) throws Exception {
        lang.packageStmt.accept(this, state);
        state.writeNewLineAndIndent();
        visitSeqList(lang.body, state);
        return null;
    }

    @Override
    public final Void visitNewExpr(NewExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("new ");
        lang.typeApply.accept(this, state);
        state.write('(');
        visitArgs(lang.args, state);
        state.write(')');
        return null;
    }

    @Override
    public final Void visitObjType(ObjType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write(lang.ident().formatValue());
        return null;
    }

    @Override
    public final Void visitOrExpr(OrExpr lang, FormatterState state) throws Exception {
        formatBinaryExpr(SymbolsAndKeywords.OR_OPER, lang.arg1, lang.arg2, state);
        return null;
    }

    @Override
    public final Void visitPackageStmt(PackageStmt lang, FormatterState state) throws Exception {
        state.write("package ");
        for (int i = 0; i < lang.path.size(); i++) {
            if (i > 0) {
                state.write('.');
            }
            lang.path.get(i).accept(this, state);
        }
        return null;
    }

    @Override
    public final Void visitProcExpr(ProcExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("proc ");
        visitProcLang(lang, state);
        return null;
    }

    private void visitProcLang(ProcLang lang, FormatterState state) throws Exception {
        state.write('(');
        visitParams(lang.params, state.inline());
        state.write(") in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        visitSeqList(lang.body.list, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
    }

    @Override
    public final Void visitProcStmt(ProcStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("proc ");
        lang.name.accept(this, state.inline());
        visitProcLang(lang, state);
        return null;
    }

    @Override
    public final Void visitProcType(ProcType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("func ");
        if (lang.name != null) {
            lang.name.accept(this, state.inline());
        }
        state.write('(');
        visitParams(lang.params, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitProductExpr(ProductExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        formatBinaryExpr(lang.oper.symbol(), lang.arg1, lang.arg2, state);
        return null;
    }

    @Override
    public final Void visitProtocolApply(ProtocolApply lang, FormatterState state) throws Exception {
        FormatterState inlineState = state.inline();
        lang.name.accept(this, inlineState);
        visitTypeArgs(lang.typeArgs, inlineState);
        return null;
    }

    @Override
    public final Void visitProtocolAskHandler(ProtocolAskHandler lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("ask ");
        lang.pat.accept(this, state.inline());
        if (lang.responseType != null) {
            state.write(" -> ");
            lang.responseType.accept(this, state.inline());
        }
        return null;
    }

    @Override
    public final Void visitProtocolStmt(ProtocolStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("protocol ");
        lang.name.accept(this, state.inline());
        visitTypeParams(lang.typeParams, state.inline());
        state.write(" = ");
        lang.body.accept(this, state);
        return null;
    }

    @Override
    public final Void visitProtocolStreamHandler(ProtocolStreamHandler lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("stream ");
        lang.pat.accept(this, state.inline());
        if (lang.responseType != null) {
            state.write(" -> ");
            lang.responseType.accept(this, state.inline());
        }
        return null;
    }

    @Override
    public final Void visitProtocolStruct(ProtocolStruct lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write('{');
        List<ProtocolHandler> list = lang.handlers;
        for (int i = 0; i < list.size(); i++) {
            ProtocolHandler next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitProtocolTellHandler(ProtocolTellHandler lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("tell ");
        lang.pat.accept(this, state.inline());
        return null;
    }

    @Override
    public final Void visitRecExpr(RecExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        if (lang.label() != null) {
            lang.label().accept(this, state.inline());
            state.write('#');
        }
        state.write('{');
        List<FieldExpr> list = lang.fields();
        for (int i = 0; i < list.size(); i++) {
            FieldExpr next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitRecPat(RecPat lang, FormatterState state) throws Exception {
        if (lang.label() != null) {
            lang.label().accept(this, state.inline());
            state.write('#');
        }
        state.write('{');
        List<FieldPat> list = lang.fields();
        for (int i = 0; i < list.size(); i++) {
            FieldPat next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
            if (lang.partialArity() && i + 1 == list.size()) {
                state.write(", ...");
            }
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitRecType(RecType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write(lang.ident().formatValue());
        return null;
    }

    @Override
    public final Void visitRecTypeExpr(RecTypeExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        if (lang.label != null) {
            lang.label.accept(this, state.inline());
            state.write('#');
        }
        state.write('{');
        if (!lang.staticFields.isEmpty()) {
            state.write("static {");
            List<FieldType> list = lang.staticFields;
            for (int i = 0; i < list.size(); i++) {
                FieldType next = list.get(i);
                if (i > 0) {
                    state.write(", ");
                }
                next.accept(this, state.inline());
            }
            state.write('}');
            if (!lang.fields.isEmpty()) {
                state.write(' ');
            }
        }
        List<FieldType> list = lang.fields;
        for (int i = 0; i < list.size(); i++) {
            FieldType next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write('}');
        return null;
    }

    @Override
    public final Void visitRelationalExpr(RelationalExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        formatBinaryExpr(lang.oper.symbol(), lang.arg1, lang.arg2, state);
        return null;
    }

    @Override
    public final Void visitRespondStmt(RespondStmt lang, FormatterState state) {
        throw new NeedsImpl();
    }

    @Override
    public final Void visitReturnStmt(ReturnStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("return");
        if (lang.value != null) {
            state.write(' ');
            lang.value.accept(this, state.inline());
        }
        return null;
    }

    @Override
    public final Void visitScalarAsExpr(ScalarAsExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        FormatAsKernelString formattable = (FormatAsKernelString) lang.value();
        state.write(formattable.formatAsKernelString());
        return null;
    }

    @Override
    public final Void visitScalarAsType(ScalarAsType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        FormatAsKernelString formattable = (FormatAsKernelString) lang.value();
        state.write(formattable.formatAsKernelString());
        return null;
    }

    @Override
    public final Void visitSelectAndApplyLang(SelectAndApplyLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        lang.selectExpr.accept(this, state);
        state.write('(');
        visitArgs(lang.args, state);
        state.write(')');
        return null;
    }

    @Override
    public final Void visitSeqLang(SeqLang lang, FormatterState state) throws Exception {
        visitSeqList(lang.list, state);
        return null;
    }

    private void visitSeqList(List<StmtOrExpr> list, FormatterState state) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            StmtOrExpr next = list.get(i);
            if (i > 0) {
                if (state.level() == FormatterState.INLINE_VALUE) {
                    StmtOrExpr prev = list.get(i - 1);
                    if (prev instanceof GroupExpr || (next instanceof UnaryExpr unaryExpr && unaryExpr.oper == UnaryOper.NEGATE)) {
                        state.write(';');
                    }
                }
                state.writeNewLineAndIndent();
            }
            next.accept(this, state);
        }
    }

    @Override
    public final Void visitSetCellValueStmt(SetCellValueStmt lang, FormatterState state) throws Exception {
        lang.leftSide.accept(this, state);
        state.write(" := ");
        lang.rightSide.accept(this, state);
        return null;
    }

    @Override
    public final Void visitSkipStmt(SkipStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("skip");
        return null;
    }

    @Override
    public final Void visitSpawnExpr(SpawnExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("spawn(");
        visitArgs(lang.args, state.inline());
        state.write(')');
        return null;
    }

    @Override
    public final Void visitSumExpr(SumExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        formatBinaryExpr(lang.oper.symbol(), lang.arg1, lang.arg2, state);
        return null;
    }

    @Override
    public final Void visitTellStmt(TellStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("handle tell ");
        lang.pat.accept(this, state.inline());
        state.write(" in");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitThrowLang(ThrowLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("throw ");
        lang.arg.accept(this, state.nextLevel());
        return null;
    }

    @Override
    public final Void visitTryLang(TryLang lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("try");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        for (CatchClause catchClause : lang.catchClauses) {
            state.writeNewLineAndIndent();
            catchClause.accept(this, state);
        }
        if (lang.finallySeq != null) {
            state.writeAfterNewLineAndIdent("finally");
            nextLevelState.writeNewLineAndIndent();
            lang.finallySeq.accept(this, nextLevelState);
        }
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

    @Override
    public final Void visitTupleExpr(TupleExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        if (lang.label() != null) {
            lang.label().accept(this, state.inline());
            state.write('#');
        }
        state.write("[");
        List<StmtOrExpr> list = lang.values();
        for (int i = 0; i < list.size(); i++) {
            StmtOrExpr next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write(']');
        return null;
    }

    @Override
    public final Void visitTuplePat(TuplePat lang, FormatterState state) throws Exception {
        if (lang.label() != null) {
            lang.label().accept(this, state.inline());
            state.write('#');
        }
        state.write("[");
        List<Pat> list = lang.values();
        for (int i = 0; i < list.size(); i++) {
            Pat next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
            if (lang.partialArity() && i + 1 == list.size()) {
                state.write(", ...");
            }
        }
        state.write(']');
        return null;
    }

    @Override
    public final Void visitTupleType(TupleType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write(lang.ident().formatValue());
        return null;
    }

    @Override
    public final Void visitTupleTypeExpr(TupleTypeExpr lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        if (lang.label != null) {
            lang.label.accept(this, state.inline());
            state.write('#');
        }
        state.write("[");
        if (!lang.staticFields.isEmpty()) {
            state.write("static {");
            List<FieldType> list = lang.staticFields;
            for (int i = 0; i < list.size(); i++) {
                FieldType next = list.get(i);
                if (i > 0) {
                    state.write(", ");
                }
                next.accept(this, state.inline());
            }
            state.write('}');
            if (!lang.values.isEmpty()) {
                state.write(' ');
            }
        }
        List<Type> list = lang.values;
        for (int i = 0; i < list.size(); i++) {
            Type next = list.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
        state.write(']');
        return null;
    }

    @Override
    public final Void visitTypeApply(TypeApply lang, FormatterState state) throws Exception {
        FormatterState inlineState = state.inline();
        lang.name.accept(this, inlineState);
        visitTypeArgs(lang.typeArgs, inlineState);
        return null;
    }

    private void visitTypeArgs(List<Type> typeArgs, FormatterState state) throws Exception {
        if (typeArgs != null && !typeArgs.isEmpty()) {
            state.write('[');
            for (int i = 0; i < typeArgs.size(); i++) {
                if (i > 0) {
                    state.write(", ");
                }
                Type arg = typeArgs.get(i);
                arg.accept(this, state.inline());
            }
            state.write(']');
        }
    }

    @Override
    public final Void visitTypeDecl(TypeDecl lang, FormatterState state) throws Exception {
        lang.name.accept(this, state.inline());
        visitTypeParams(lang.typeParams, state);
        return null;
    }

    @Override
    public final Void visitTypeParam(TypeParam lang, FormatterState state) throws Exception {
        lang.typeDecl.accept(this, state.inline());
        if (lang.constraintOper != null) {
            state.write(' ');
            state.write(lang.constraintOper.name());
            state.write(' ');
            lang.constraintArg.accept(this, state.inline());
        }
        return null;
    }

    private void visitTypeParams(List<TypeParam> typeParams, FormatterState state) throws Exception {
        if (!typeParams.isEmpty()) {
            state.write("[");
            for (TypeParam param : typeParams) {
                param.accept(this, state.inline());
            }
            state.write("]");
        }
    }

    @Override
    public final Void visitTypeStmt(TypeStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("type ");
        lang.typeDecl.accept(this, state);
        state.write(" = ");
        lang.body.accept(this, state);
        return null;
    }

    @Override
    public final Void visitUnaryExpr(UnaryExpr lang, FormatterState state) throws Exception {
        state.write(lang.oper.symbol());
        lang.arg.accept(this, state);
        return null;
    }

    @Override
    public final Void visitUnifyStmt(UnifyStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        lang.leftSide.accept(this, state.inline());
        state.write(" = ");
        lang.rightSide.accept(this, state);
        return null;
    }

    @Override
    public final Void visitUnionType(UnionType lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        lang.arg1.accept(this, state.inline());
        state.write(" | ");
        lang.arg2.accept(this, state.inline());
        return null;
    }

    private void visitVarDecls(List<VarDecl> varDecls, FormatterState state) throws Exception {
        for (int i = 0; i < varDecls.size(); i++) {
            VarDecl next = varDecls.get(i);
            if (i > 0) {
                state.write(", ");
            }
            next.accept(this, state.inline());
        }
    }

    @Override
    public final Void visitVarStmt(VarStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("var ");
        visitVarDecls(lang.varDecls, state);
        return null;
    }

    @Override
    public final Void visitWhileStmt(WhileStmt lang, FormatterState state) throws Exception {
        maybeWriteMeta(lang, state);
        state.write("while ");
        lang.cond.accept(this, state.inline());
        state.write(" do");
        FormatterState nextLevelState = state.nextLevel();
        nextLevelState.writeNewLineAndIndent();
        lang.body.accept(this, nextLevelState);
        state.writeAfterNewLineAndIdent("end");
        return null;
    }

}
