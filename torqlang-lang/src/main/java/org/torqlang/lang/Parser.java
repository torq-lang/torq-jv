/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.IntegerCounter;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

import static org.torqlang.lang.MessageText.*;
import static org.torqlang.lang.SymbolsAndKeywords.*;
import static org.torqlang.util.ListTools.last;

public final class Parser {

    private final Lexer lexer;

    private LexerToken currentToken;

    public Parser(String source) {
        this.lexer = new Lexer(source);
    }

    public static Lang parse(String source) {
        Parser p = new Parser(source);
        return p.parse();
    }

    private static SourceSpan sourceSpanForSeq(List<? extends StmtOrExpr> seq) {
        return seq.get(0).adjoin(last(seq));
    }

    private static String unquoteString(String source, int begin, int end) {
        begin = begin + 1;
        end = end - 1;
        StringBuilder sb = new StringBuilder((end - begin) * 2);
        int i = begin;
        while (i < end) {
            char c1 = source.charAt(i);
            if (c1 == '\\') {
                char c2 = source.charAt(i + 1);
                if (c2 != 'u') {
                    if (c2 == 'r') {
                        c1 = '\r';
                    } else if (c2 == 'n') {
                        c1 = '\n';
                    } else if (c2 == 't') {
                        c1 = '\t';
                    } else if (c2 == 'f') {
                        c1 = '\f';
                    } else if (c2 == 'b') {
                        c1 = '\b';
                    } else if (c2 == '\\') {
                        c1 = '\\';
                    } else if (c2 == '\'') {
                        c1 = '\'';
                    } else if (c2 == '"') {
                        c1 = '"';
                    } else {
                        throw new IllegalArgumentException("Invalid escape sequence: " + c1 + c2);
                    }
                    sb.append(c1);
                    i += 2;
                } else {
                    int code = Integer.parseInt("" + source.charAt(i + 2) + source.charAt(i + 3) +
                        source.charAt(i + 4) + source.charAt(i + 5), 16);
                    sb.append(Character.toChars(code));
                    i += 6;
                }
            } else {
                sb.append(c1);
                i++;
            }
        }
        return sb.toString();
    }

    private LexerToken acceptEndToken() {
        LexerToken current = currentToken;
        if (!current.isKeyword(END_VALUE)) {
            throw new ParserError(END_EXPECTED, current);
        }
        nextToken();
        return current;
    }

    private void assertCurrentAtKeyword(String message, String keywordValue) {
        if (!currentToken.isKeyword(keywordValue)) {
            throw new ParserError(message, currentToken);
        }
    }

    private boolean includesLineBreakBetween(SourceSpan first, LexerToken second) {
        String source = first.source();
        int start = first.sourceEnd() - 1;
        int stop = Math.min(second.sourceBegin(), source.length());
        for (int i = start; i < stop; i++) {
            if (source.charAt(i) == '\n') {
                return true;
            }
        }
        return false;
    }

    public final boolean isEof() {
        return currentToken.isEof();
    }

    private UnaryOper negateOrNotOperFor(LexerToken operToken) {
        if (operToken.isOneCharSymbol()) {
            char firstOperChar = operToken.firstChar();
            if (firstOperChar == SUBTRACT_OPER_CHAR) {
                return UnaryOper.NEGATE;
            }
            if (firstOperChar == NOT_OPER_CHAR) {
                return UnaryOper.NOT;
            }
        }
        return null;
    }

    private LexerToken nextToken() {
        currentToken = lexer.nextToken(true);
        return currentToken;
    }

    public final StmtOrExpr parse() {
        nextToken();
        StmtOrExpr answer = parseStmtOrExpr();
        if (!currentToken.isEof()) {
            throw new ParserError(UNEXPECTED_TOKEN, currentToken);
        }
        return answer;
    }

    private StmtOrExpr parseAccess() {
        StmtOrExpr construct = parseConstruct();
        if (construct != null) {
            return construct;
        }
        LexerToken operToken = currentToken;
        if (!operToken.isOneCharSymbol(ACCESS_CELL_VALUE_OPER_CHAR)) {
            return null;
        }
        nextToken(); // accept '@' token
        StmtOrExpr right = parseAccess();
        if (right == null) {
            throw new ParserError(EXPR_EXPECTED, currentToken);
        }
        return new UnaryExpr(UnaryOper.ACCESS, right, operToken.adjoin(right));
    }

    private ActExpr parseAct() {
        LexerToken actToken = currentToken;
        nextToken(); // accept 'act' token
        SeqLang seq = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new ActExpr(seq, actToken.adjoin(endToken));
    }

    private ActorLang parseActor() {
        LexerToken actorToken = currentToken;
        Ident name = null;
        LexerToken current = nextToken();
        if (current.isIdent()) {
            name = tokenToIdent(current);
            current = nextToken(); // accept IDENT
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> formalArgs = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        List<StmtOrExpr> body = new ArrayList<>();
        while (true) {
            if (currentToken.isWeakKeyword(HANDLE_VALUE)) {
                LexerToken handleToken = currentToken;
                nextToken(); // accept 'handle' token
                if (currentToken.isWeakKeyword(ASK_VALUE)) {
                    body.add(parseAsk(handleToken));
                } else if (currentToken.isWeakKeyword(TELL_VALUE)) {
                    body.add(parseTell(handleToken));
                } else {
                    throw new ParserError(ASK_OR_TELL_EXPECTED, currentToken);
                }
            } else {
                StmtOrExpr next = parseStmtOrExpr();
                if (next != null) {
                    body.add(next);
                } else {
                    break;
                }
            }
        }
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new ActorStmt(name, formalArgs, body, actorToken.adjoin(endToken));
        } else {
            return new ActorExpr(formalArgs, body, actorToken.adjoin(endToken));
        }
    }

    private StmtOrExpr parseAnd() {
        StmtOrExpr answer = parseRelational();
        LexerToken operToken = currentToken;
        while (operToken.isTwoCharSymbol(AND_OPER)) {
            nextToken(); // accept '&&' token
            StmtOrExpr right = parseRelational();
            if (right == null) {
                throw new ParserError(EXPR_EXPECTED, currentToken);
            }
            answer = new AndExpr(answer, right, answer.adjoin(right));
            operToken = currentToken;
        }
        return answer;
    }

    private List<StmtOrExpr> parseArgList() {
        List<StmtOrExpr> args = new ArrayList<>();
        StmtOrExpr arg = parseStmtOrExpr();
        while (arg != null) {
            args.add(arg);
            LexerToken current = currentToken;
            if (!current.isOneCharSymbol(COMMA_CHAR)) {
                break;
            }
            nextToken(); // accept ',' token
            arg = parseStmtOrExpr();
        }
        return args;
    }

    private AskStmt parseAsk(LexerToken handleToken) {
        LexerToken current = nextToken(); // accept 'ask' token
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PATTERN_EXPECTED, current);
        }
        TypeAnno responseType = parseReturnTypeAnno();
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new AskStmt(pat, body, responseType, handleToken.adjoin(endToken));
    }

    private StmtOrExpr parseAssign() {
        StmtOrExpr left = parseOr();
        LexerToken operToken = currentToken;
        if (operToken.isOneCharSymbol()) {
            if (operToken.firstChar() == UNIFY_OPER_CHAR) {
                nextToken(); // accept '=' token
                StmtOrExpr right = parseOr();
                if (right == null) {
                    throw new ParserError(EXPR_EXPECTED, currentToken);
                }
                return new UnifyStmt(left, right, left.adjoin(right));
            }
        } else if (operToken.isTwoCharSymbol()) {
            if (operToken.substringEquals(ASSIGN_CELL_VALUE_OPER)) {
                nextToken(); // accept ':=' token
                StmtOrExpr right = parseOr();
                if (right == null) {
                    throw new ParserError(EXPR_EXPECTED, currentToken);
                }
                return new SetCellValueStmt(left, right, left.adjoin(right));
            }
        }
        return left;
    }

    private BeginLang parseBegin() {
        LexerToken beginToken = currentToken;
        nextToken(); // accept 'begin' token
        SeqLang seq = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new BeginLang(seq, beginToken.adjoin(endToken));
    }

    private CaseLang parseCase() {
        LexerToken caseToken = currentToken;
        LexerToken current = nextToken(); // accept 'case' token
        StmtOrExpr arg = parseStmtOrExpr();
        if (arg == null) {
            throw new ParserError(EXPR_EXPECTED, current);
        }
        assertCurrentAtKeyword(OF_EXPECTED, OF_VALUE);
        CaseClause caseClause = parseCaseClause();
        List<CaseClause> altCaseClauses = new ArrayList<>();
        current = currentToken;
        while (current.isKeyword(OF_VALUE)) {
            CaseClause altCaseClause = parseCaseClause();
            altCaseClauses.add(altCaseClause);
            current = currentToken;
        }
        SeqLang elseSeq = null;
        if (current.isKeyword(ELSE_VALUE)) {
            nextToken(); // accept 'else' token
            elseSeq = parseSeq();
        }
        LexerToken endToken = acceptEndToken();
        return new CaseLang(arg, caseClause, altCaseClauses, elseSeq, caseToken.adjoin(endToken));
    }

    private CaseClause parseCaseClause() {
        LexerToken ofToken = currentToken;
        LexerToken current = nextToken(); // accept 'of' token
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PATTERN_EXPECTED, current);
        }
        StmtOrExpr guard = null;
        current = currentToken;
        if (current.isKeyword(WHEN_VALUE)) {
            nextToken(); // accept 'when'
            guard = parseStmtOrExpr();
        }
        assertCurrentAtKeyword(THEN_EXPECTED, THEN_VALUE);
        nextToken(); // accept THEN
        SeqLang body = parseSeq();
        return new CaseClause(pat, guard, body, ofToken.adjoin(body));
    }

    private StmtOrExpr parseConstruct() {
        Expr valueOrIdentAsExpr = parseValueOrIdentAsExpr();
        if (valueOrIdentAsExpr != null) {
            return valueOrIdentAsExpr;
        }
        LexerToken current = currentToken;
        if (current.isEof()) {
            return null;
        }
        if (current.isOneCharSymbol()) {
            char firstChar = current.firstChar();
            if (firstChar == L_PAREN_CHAR) {
                return parseGroup();
            }
        }
        if (current.isKeyword()) {
            if (current.substringEquals(VAR_VALUE)) {
                nextToken(); // accept 'var' token
                List<VarDecl> varDecls = parseVarDecls();
                return new VarStmt(varDecls, current.adjoin(last(varDecls)));
            }
            if (current.substringEquals(IF_VALUE)) {
                return parseIf();
            }
            if (current.substringEquals(FOR_VALUE)) {
                return parseFor();
            }
            if (current.substringEquals(WHILE_VALUE)) {
                return parseWhile();
            }
            if (current.substringEquals(CASE_VALUE)) {
                return parseCase();
            }
            if (current.substringEquals(FUNC_VALUE)) {
                return parseFunc();
            }
            if (current.substringEquals(PROC_VALUE)) {
                return parseProc();
            }
            if (current.substringEquals(ACT_VALUE)) {
                return parseAct();
            }
            if (current.substringEquals(SPAWN_VALUE)) {
                return parseSpawn();
            }
            if (current.substringEquals(ACTOR_VALUE)) {
                return parseActor();
            }
            if (current.substringEquals(IMPORT_VALUE)) {
                return parseImport();
            }
            if (current.substringEquals(BEGIN_VALUE)) {
                return parseBegin();
            }
            if (current.substringEquals(LOCAL_VALUE)) {
                return parseLocal();
            }
            if (current.substringEquals(THROW_VALUE)) {
                return parseThrow();
            }
            if (current.substringEquals(TRY_VALUE)) {
                return parseTry();
            }
            if (current.substringEquals(SELF_VALUE)) {
                nextToken(); // accept 'self' token
                return new IdentAsExpr(Ident.$SELF, current);
            }
            if (current.substringEquals(BREAK_VALUE)) {
                nextToken(); // accept 'break' token
                return new BreakStmt(current);
            }
            if (current.substringEquals(CONTINUE_VALUE)) {
                nextToken(); // accept 'continue' token
                return new ContinueStmt(current);
            }
            if (current.substringEquals(RETURN_VALUE)) {
                return parseReturn();
            }
            if (current.substringEquals(SKIP_VALUE)) {
                nextToken(); // accept 'skip' token
                return new SkipStmt(current);
            }
        }
        return null;
    }

    private FieldExpr parseFieldExpr() {
        StmtOrExpr featureExpr = parseStmtOrExpr();
        if (featureExpr == null) {
            return null;
        }
        LexerToken current = nextToken(); // accept ':'
        StmtOrExpr valueExpr = parseStmtOrExpr();
        if (valueExpr == null) {
            throw new ParserError(INVALID_VALUE_EXPR, current);
        }
        return new FieldExpr(featureExpr, valueExpr, featureExpr.adjoin(valueExpr));
    }

    private FieldPat parseFieldPat(IntegerCounter nextImpliedFeature) {
        Pat featureOrValuePat = parsePat();
        if (featureOrValuePat == null) {
            return null;
        }
        FeaturePat featurePat;
        Pat valuePat;
        LexerToken current = currentToken;
        if (current.isOneCharSymbol(COLON_CHAR)) {
            current = nextToken(); // accept ':'
            if (!(featureOrValuePat instanceof FeaturePat featurePatParsed)) {
                throw new ParserError(INVALID_FEATURE_PAT, featureOrValuePat);
            }
            featurePat = featurePatParsed;
            valuePat = parsePat();
            if (valuePat == null) {
                throw new ParserError(INVALID_VALUE_PAT, current);
            }
        } else {
            int nextFeature = nextImpliedFeature.getAndAdd(1);
            featurePat = new IntAsPat(Int32.of(nextFeature), featureOrValuePat.toSourceBegin());
            valuePat = featureOrValuePat;
        }
        return new FieldPat(featurePat, valuePat, featurePat.adjoin(valuePat));
    }

    private ForStmt parseFor() {
        LexerToken forToken = currentToken;
        LexerToken current = nextToken(); // accept 'for' token
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PATTERN_EXPECTED, current);
        }
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        StmtOrExpr iter = parseStmtOrExpr();
        current = currentToken;
        if (iter == null) {
            throw new ParserError(EXPR_EXPECTED, current);
        }
        if (!current.isKeyword(DO_VALUE)) {
            throw new ParserError(DO_EXPECTED, current);
        }
        nextToken(); // accept 'do' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new ForStmt(pat, iter, body, forToken.adjoin(endToken));
    }

    private FuncLang parseFunc() {
        LexerToken funcToken = currentToken;
        Ident name = null;
        LexerToken current = nextToken(); // accept 'func' token
        if (current.isIdent()) {
            name = tokenToIdent(current);
            current = nextToken(); // accept IDENT
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> formalArgs = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        TypeAnno returnType = parseReturnTypeAnno();
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new FuncStmt(name, formalArgs, returnType, body, funcToken.adjoin(endToken));
        } else {
            return new FuncExpr(formalArgs, returnType, body, funcToken.adjoin(endToken));
        }
    }

    private StmtOrExpr parseGroup() {
        LexerToken groupBegin = currentToken;
        nextToken(); // accept '(' token
        SeqLang seq = parseSeq();
        LexerToken groupEnd = currentToken;
        if (!groupEnd.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, groupEnd);
        }
        nextToken(); // accept ')' token
        return new GroupExpr(seq, groupBegin.adjoin(groupEnd));
    }

    private IfLang parseIf() {
        IfClause ifClause = parseIfClause();
        LexerToken current = currentToken;
        List<IfClause> altIfClauses = new ArrayList<>();
        while (current.isKeyword(ELSEIF_VALUE)) {
            IfClause altIfClause = parseIfClause();
            altIfClauses.add(altIfClause);
            current = currentToken;
        }
        SeqLang elseSeq = null;
        if (current.isKeyword(ELSE_VALUE)) {
            nextToken(); // accept 'else' token
            elseSeq = parseSeq();
        }
        LexerToken endToken = acceptEndToken();
        return new IfLang(ifClause, altIfClauses, elseSeq, ifClause.adjoin(endToken));
    }

    private IfClause parseIfClause() {
        LexerToken ifOrElseIfToken = currentToken;
        LexerToken current = nextToken(); // accept 'if' or 'elseif' token
        StmtOrExpr condition = parseStmtOrExpr();
        if (condition == null) {
            throw new ParserError(EXPR_EXPECTED, current);
        }
        assertCurrentAtKeyword(THEN_EXPECTED, THEN_VALUE);
        nextToken(); // accept THEN
        SeqLang body = parseSeq();
        return new IfClause(condition, body, ifOrElseIfToken.adjoin(body));
    }

    private ImportStmt parseImport() {
        LexerToken importToken = currentToken;
        LexerToken current = nextToken(); // accept 'import' token
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, current);
        }
        StringBuilder qualifier = new StringBuilder();
        List<ImportName> names = new ArrayList<>();
        LexerToken previous = current;
        current = nextToken();
        // Parse qualifier
        while (current.isOneCharSymbol(DOT_OPER_CHAR) || current.isOneCharSymbol(L_BRACKET_CHAR)) {
            if (!qualifier.isEmpty()) {
                qualifier.append('.');
            }
            qualifier.append(previous.substring());
            if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
                break;
            }
            current = nextToken();
            if (!current.isIdent()) {
                throw new ParserError(IDENT_EXPECTED, current);
            }
            previous = current;
            current = nextToken();
        }
        // Current token is either the '[' or one token past a name
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            current = nextToken(); // accept '['
            while (current.isIdent()) {
                Str name = Str.of(current.substring());
                current = nextToken(); // accept Ident
                if (current.isWeakKeyword(AS_VALUE)) {
                    current = nextToken(); // accept 'as'
                    if (!current.isIdent()) {
                        throw new ParserError(IDENT_EXPECTED, current);
                    }
                    Str alias = Str.of(current.substring());
                    current = nextToken(); // accept alias
                    names.add(new ImportName(name, alias));
                } else {
                    names.add(new ImportName(name));
                }
                if (current.isOneCharSymbol(COMMA_CHAR)) {
                    current = nextToken();
                }
            }
            if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
                throw new ParserError(R_BRACKET_EXPECTED, current);
            }
            current = nextToken(); // accept ']'
        } else {
            names.add(new ImportName(Str.of(previous.substring())));
        }
        // Current token is now one past the import expression
        return new ImportStmt(Str.of(qualifier.toString()), names, importToken.adjoin(current));
    }

    private LocalLang parseLocal() {
        LexerToken localToken = currentToken;
        nextToken(); // accept 'local' token
        List<VarDecl> varDecls = parseVarDecls();
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new LocalLang(varDecls, body, localToken.adjoin(endToken));
    }

    private StmtOrExpr parseOr() {
        StmtOrExpr answer = parseAnd();
        LexerToken operToken = currentToken;
        while (operToken.isTwoCharSymbol(OR_OPER)) {
            nextToken(); // accept '||' token
            StmtOrExpr right = parseAnd();
            if (right == null) {
                throw new ParserError(EXPR_EXPECTED, currentToken);
            }
            answer = new OrExpr(answer, right, answer.adjoin(right));
            operToken = currentToken;
        }
        return answer;
    }

    private Pat parsePat() {
        LexerToken current = currentToken;
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseRecPat(null);
            }
            if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseTuplePat(null);
            }
            if (current.firstCharEquals(IDENT_ESC_CHAR)) {
                LexerToken next = nextToken(); // accept '~'
                if (!next.isIdent()) {
                    throw new ParserError(IDENT_EXPECTED, next);
                }
                Ident ident = tokenToIdent(next);
                IdentAsPat identAsPat = new IdentAsPat(ident, true, current.adjoin(next));
                next = nextToken(); // accept IDENT
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseRecOrTuplePat(identAsPat);
                }
                return identAsPat;
            }
            return null;
        }
        if (current.isStr()) {
            LexerToken next = nextToken(); // accept Str token
            String substring = unquoteString(current.source(), current.sourceBegin(), current.sourceEnd());
            StrAsPat strAsPat = new StrAsPat(Str.of(substring), current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTuplePat(strAsPat);
            }
            return strAsPat;
        }
        if (current.isIdent()) {
            LexerToken next = nextToken(); // accept Ident token
            Ident ident = tokenToIdent(current);
            if (!next.isTwoCharSymbol(TYPE_OPER)) {
                return new IdentAsPat(ident, false, current);
            }
            next = nextToken(); // accept '::' and get IDENT
            if (!next.isIdent()) {
                throw new ParserError(IDENT_EXPECTED, next);
            }
            nextToken(); // accept type Ident
            Ident identType = tokenToIdent(next);
            return new IdentAsPat(ident, false,
                new TypeAnno(identType, next), current.adjoin(next));
        }
        if (current.isInt()) {
            nextToken(); // accept Int token
            String symbolText = current.substring();
            return new IntAsPat(symbolText, current);
        }
        if (current.substringEquals(TRUE_VALUE)) {
            LexerToken next = nextToken();  // accept 'true' token
            BoolAsPat boolAsPat = new BoolAsPat(Bool.TRUE, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTuplePat(boolAsPat);
            }
            return boolAsPat;
        }
        if (current.substringEquals(FALSE_VALUE)) {
            LexerToken next = nextToken();  // accept 'false' token
            BoolAsPat boolAsPat = new BoolAsPat(Bool.FALSE, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTuplePat(boolAsPat);
            }
            return boolAsPat;
        }
        if (current.substringEquals(NULL_VALUE)) {
            LexerToken next = nextToken(); // accept 'null' token
            NullAsPat nullAsPat = new NullAsPat(current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTuplePat(nullAsPat);
            }
            return nullAsPat;
        }
        if (current.substringEquals(EOF_VALUE)) {
            LexerToken next = nextToken(); // accept 'eof' token
            EofAsPat eofAsPat = new EofAsPat(current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTuplePat(eofAsPat);
            }
            return eofAsPat;
        }
        return null;
    }

    private List<Pat> parsePatList() {
        List<Pat> args = new ArrayList<>();
        Pat arg = parsePat();
        if (arg != null) {
            args.add(arg);
        }
        LexerToken current = currentToken;
        while (current.isOneCharSymbol(COMMA_CHAR)) {
            nextToken(); // accept ',' token
            arg = parsePat();
            current = currentToken;
            if (arg == null) {
                throw new ParserError(PATTERN_EXPECTED, current);
            }
            args.add(arg);
        }
        return args;
    }

    private ProcLang parseProc() {
        LexerToken procToken = currentToken;
        Ident name = null;
        LexerToken current = nextToken(); // accept 'proc' token
        if (current.isIdent()) {
            name = tokenToIdent(current);
            current = nextToken(); // accept IDENT
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> formalArgs = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new ProcStmt(name, formalArgs, body, procToken.adjoin(endToken));
        } else {
            return new ProcExpr(formalArgs, body, procToken.adjoin(endToken));
        }
    }

    private StmtOrExpr parseProduct() {
        StmtOrExpr answer = parseUnary();
        LexerToken operToken = currentToken;
        ProductOper productOper = productOperFor(operToken);
        while (productOper != null) {
            nextToken(); // accept '*', '/' or '%' token
            StmtOrExpr right = parseUnary();
            if (right == null) {
                throw new ParserError(EXPR_EXPECTED, currentToken);
            }
            answer = new ProductExpr(answer, productOper, right, answer.adjoin(right));
            operToken = currentToken;
            productOper = productOperFor(operToken);
        }
        return answer;
    }

    private RecExpr parseRecExpr(Expr label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '{' token
        List<FieldExpr> fieldExprs = new ArrayList<>();
        FieldExpr fieldExpr = parseFieldExpr();
        if (fieldExpr != null) {
            fieldExprs.add(fieldExpr);
        }
        LexerToken current = currentToken;
        while (current.isOneCharSymbol(COMMA_CHAR)) {
            nextToken(); // accept ',' token
            fieldExpr = parseFieldExpr();
            current = currentToken;
            if (fieldExpr != null) {
                fieldExprs.add(fieldExpr);
            } else {
                throw new ParserError(FIELD_EXPECTED, current);
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new RecExpr(label, fieldExprs, recSpan);
    }

    private Expr parseRecOrTupleExpr(LabelExpr label) {
        LexerToken current = nextToken(); // accept '#' token
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseRecExpr(label);
            } else if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseTupleExpr(label);
            }
        }
        throw new ParserError(L_BRACE_OR_L_BRACKET_EXPECTED, current);
    }

    private Pat parseRecOrTuplePat(LabelPat label) {
        LexerToken current = nextToken(); // accept '#' token
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseRecPat(label);
            } else if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseTuplePat(label);
            }
        }
        throw new ParserError(L_BRACE_OR_L_BRACKET_EXPECTED, current);
    }

    private RecPat parseRecPat(LabelPat label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '{' token
        IntegerCounter nextImpliedFeature = new IntegerCounter(0);
        boolean partialArity = false;
        List<FieldPat> fieldPats = new ArrayList<>();
        FieldPat fieldPat = parseFieldPat(nextImpliedFeature);
        if (fieldPat != null) {
            fieldPats.add(fieldPat);
        }
        LexerToken current = currentToken;
        while (current.isOneCharSymbol(COMMA_CHAR)) {
            nextToken(); // accept ',' token
            fieldPat = parseFieldPat(nextImpliedFeature);
            current = currentToken;
            if (fieldPat != null) {
                fieldPats.add(fieldPat);
            } else if (current.isThreeCharSymbol(PARTIAL_ARITY_OPER)) {
                current = nextToken(); // accept '...' token
                partialArity = true;
            } else {
                throw new ParserError(FIELD_EXPECTED, current);
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new RecPat(label, fieldPats, partialArity, recSpan);
    }

    private StmtOrExpr parseRelational() {
        StmtOrExpr answer = parseSum();
        LexerToken operToken = currentToken;
        RelationalOper relationalOper = relationalOperFor(operToken);
        while (relationalOper != null) {
            nextToken(); // accept '==', '!=', '<', '<=', '>' or '>=' token
            StmtOrExpr right = parseSum();
            if (right == null) {
                throw new ParserError(EXPR_EXPECTED, currentToken);
            }
            answer = new RelationalExpr(answer, relationalOper, right, answer.adjoin(right));
            operToken = currentToken;
            relationalOper = relationalOperFor(operToken);
        }
        return answer;
    }

    private ReturnStmt parseReturn() {
        LexerToken returnToken = currentToken;
        nextToken(); // accept 'return' token
        StmtOrExpr expr = parseStmtOrExpr();
        if (expr == null) {
            return new ReturnStmt(null, returnToken);
        }
        return new ReturnStmt(expr, returnToken.adjoin(expr));
    }

    private TypeAnno parseReturnTypeAnno() {
        LexerToken current = currentToken;
        TypeAnno typeAnno = null;
        if (current.isTwoCharSymbol(RETURN_TYPE_OPER)) {
            LexerToken typeOper = current;
            current = nextToken(); // accept '->' and get IDENT
            if (!current.isIdent()) {
                throw new ParserError(IDENT_EXPECTED, current);
            }
            String identText = current.substring();
            Ident typeIdent = Ident.create(identText);
            typeAnno = new TypeAnno(typeIdent, typeOper.adjoin(current));
            nextToken(); // accept Ident and position for next
        }
        return typeAnno;
    }

    /*
     * Because parenthesis '(...)' and brackets '[...]' are also used for grouping and tuple literals, respectively,
     * we must disambiguate their usage. When parenthesis or brackets are applied to a left-side operand, then at
     * least the opening symbol must appear on the same line as the operand. For example, 'aVariable(arg)[feat]' cannot
     * be broken such that the '(' or '[' appear first on the next line.
     */
    private StmtOrExpr parseSelectOrApply() {
        StmtOrExpr answer = parseAccess();
        if (answer == null) {
            return null;
        }
        LexerToken operToken = currentToken;
        SelectOrApply selectOrApply = selectOrApplyFor(answer.toSourceEnd(), operToken);
        while (selectOrApply != null) {
            nextToken(); // accept '.', '[', or '(' token
            if (selectOrApply == SelectOrApply.DOT) {
                StmtOrExpr right = parseAccess();
                if (right == null) {
                    throw new ParserError(SELECTOR_EXPECTED, currentToken);
                }
                answer = new DotSelectExpr(answer, right, answer.adjoin(right));
            } else if (selectOrApply == SelectOrApply.INDEX) {
                StmtOrExpr featureExpr = parseStmtOrExpr();
                if (featureExpr == null) {
                    throw new ParserError(EXPR_EXPECTED, currentToken);
                }
                LexerToken current = currentToken;
                if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
                    throw new ParserError(R_BRACKET_EXPECTED, current);
                }
                nextToken(); // accept ']' token
                answer = new IndexSelectExpr(answer, featureExpr, answer.adjoin(current));
            } else {
                List<StmtOrExpr> args = parseArgList();
                LexerToken current = currentToken;
                if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
                    throw new ParserError(R_PAREN_EXPECTED, current);
                }
                nextToken(); // accept ')' token
                if (answer instanceof SelectExpr selectExpr) {
                    answer = new SelectAndApplyLang(selectExpr, args, answer.adjoin(current));
                } else {
                    answer = new ApplyLang(answer, args, answer.adjoin(current));
                }
            }
            operToken = currentToken;
            selectOrApply = selectOrApplyFor(answer.toSourceEnd(), operToken);
        }
        return answer;
    }

    /*
     * Parse a sequence of statements and/or expressions until we reach a terminating token. Some examples of
     * terminating tokens are END_OF_FILE, END, ELSE, and ELSEIF.
     */
    private SeqLang parseSeq() {
        List<StmtOrExpr> list = new ArrayList<>();
        StmtOrExpr next = parseStmtOrExpr();
        while (next != null) {
            list.add(next);
            next = parseStmtOrExpr();
        }
        if (list.isEmpty()) {
            // If list is empty, no tokens were accepted
            LexerToken unrecognizedToken = currentToken;
            throw new ParserError(STMT_OR_EXPR_EXPECTED, unrecognizedToken);
        }
        return new SeqLang(list, sourceSpanForSeq(list));
    }

    private StmtOrExpr parseStmtOrExpr() {
        StmtOrExpr assign = parseAssign();
        while (currentToken.isOneCharSymbol(SEMICOLON_CHAR)) {
            nextToken();
        }
        return assign;
    }

    private SpawnExpr parseSpawn() {
        LexerToken spawnToken = currentToken;
        LexerToken current = nextToken(); // accept 'spawn' token
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<StmtOrExpr> args = parseArgList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        return new SpawnExpr(args, spawnToken.adjoin(current));
    }

    private StmtOrExpr parseSum() {
        StmtOrExpr answer = parseProduct();
        LexerToken operToken = currentToken;
        SumOper sumOper = sumOperFor(operToken);
        while (sumOper != null) {
            nextToken(); // accept '+' or '-' token
            StmtOrExpr right = parseProduct();
            if (right == null) {
                throw new ParserError(EXPR_EXPECTED, currentToken);
            }
            answer = new SumExpr(answer, sumOper, right, answer.adjoin(right));
            operToken = currentToken;
            sumOper = sumOperFor(operToken);
        }
        return answer;
    }

    private TellStmt parseTell(LexerToken handleToken) {
        LexerToken current = nextToken(); // accept 'tell' token
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PATTERN_EXPECTED, current);
        }
        assertCurrentAtKeyword(IN_EXPECTED, IN_VALUE);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new TellStmt(pat, body, handleToken.adjoin(endToken));
    }

    private ThrowLang parseThrow() {
        LexerToken throwToken = currentToken;
        nextToken(); // accept 'throw' token
        StmtOrExpr expr = parseStmtOrExpr();
        if (expr == null) {
            throw new ParserError(EXPR_EXPECTED, currentToken);
        }
        return new ThrowLang(expr, throwToken.adjoin(expr));
    }

    private TryLang parseTry() {
        LexerToken tryToken = currentToken;
        nextToken(); // accept 'try' token
        SeqLang seq = parseSeq();
        LexerToken current = currentToken;
        List<CatchClause> catchClauses = new ArrayList<>();
        while (current.isKeyword(CATCH_VALUE)) {
            LexerToken catchToken = current;
            nextToken(); // accept 'catch' token
            Pat pat = parsePat();
            if (pat == null) {
                throw new ParserError(PATTERN_EXPECTED, current);
            }
            assertCurrentAtKeyword(THEN_EXPECTED, THEN_VALUE);
            nextToken(); // accept 'then' token
            SeqLang catchSeq = parseSeq();
            catchClauses.add(new CatchClause(pat, catchSeq, catchToken.adjoin(catchSeq)));
            current = currentToken;
        }
        SeqLang finallyStmt = null;
        if (current.isKeyword(FINALLY_VALUE)) {
            nextToken(); // accept 'finally' token
            finallyStmt = parseSeq();
        }
        LexerToken endToken = acceptEndToken();
        if (catchClauses.isEmpty() && finallyStmt == null) {
            throw new ParserError(CATCH_OR_FINALLY_EXPECTED, endToken);
        }
        return new TryLang(seq, catchClauses, finallyStmt, tryToken.adjoin(endToken));
    }

    private TupleExpr parseTupleExpr(Expr label) {
        // tupleValue: '[' (expr (',' expr)*)? ']';
        LexerToken tupleToken = currentToken;
        nextToken(); // accept '[' token
        List<StmtOrExpr> valueExprs = parseArgList();
        LexerToken current = currentToken;
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        SourceSpan tupleSpan = label == null ? tupleToken.adjoin(current) : label.adjoin(current);
        return new TupleExpr(label, valueExprs, tupleSpan);
    }

    private TuplePat parseTuplePat(LabelPat label) {
        LexerToken tupleToken = currentToken;
        nextToken(); // accept '[' token
        boolean partialArity = false;
        List<Pat> valuePats = new ArrayList<>();
        Pat pat = parsePat();
        if (pat != null) {
            valuePats.add(pat);
        }
        LexerToken current = currentToken;
        while (current.isOneCharSymbol(COMMA_CHAR)) {
            nextToken(); // accept ',' token
            pat = parsePat();
            current = currentToken;
            if (pat != null) {
                valuePats.add(pat);
            } else if (current.isThreeCharSymbol(PARTIAL_ARITY_OPER)) {
                current = nextToken(); // accept '...' token
                partialArity = true;
            } else {
                throw new ParserError(PATTERN_EXPECTED, current);
            }
        }
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        SourceSpan tupleSpan = label == null ? tupleToken.adjoin(current) : label.adjoin(current);
        return new TuplePat(label, valuePats, partialArity, tupleSpan);
    }

    private StmtOrExpr parseUnary() {
        StmtOrExpr selectOrApply = parseSelectOrApply();
        if (selectOrApply != null) {
            return selectOrApply;
        }
        LexerToken operToken = currentToken;
        UnaryOper unaryOper = negateOrNotOperFor(operToken);
        if (unaryOper == null) {
            return null;
        }
        nextToken(); // accept '-' or '!' token
        StmtOrExpr right = parseUnary();
        if (right == null) {
            throw new ParserError(EXPR_EXPECTED, currentToken);
        }
        return new UnaryExpr(unaryOper, right, operToken.adjoin(right));
    }

    private Expr parseValueOrIdentAsExpr() {
        LexerToken current = currentToken;
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseRecExpr(null);
            }
            if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseTupleExpr(null);
            }
            return null;
        }
        if (current.isStr()) {
            LexerToken next = nextToken(); // accept STR token
            String substring = unquoteString(current.source(), current.sourceBegin(), current.sourceEnd());
            StrAsExpr strAsExpr = new StrAsExpr(Str.of(substring), current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTupleExpr(strAsExpr);
            }
            return strAsExpr;
        }
        if (current.isIdent()) {
            LexerToken next = nextToken(); // accept IDENT token
            Ident ident = tokenToIdent(current);
            IdentAsExpr identAsExpr = new IdentAsExpr(ident, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseRecOrTupleExpr(identAsExpr);
            }
            return identAsExpr;
        }
        if (current.isInt()) {
            nextToken(); // accept Int token
            String symbolText = current.substring();
            return new IntAsExpr(symbolText, current);
        }
        if (current.isFlt()) {
            nextToken();  // accept Flt token
            String symbolText = current.substring();
            return new FltAsExpr(symbolText, current);
        }
        if (current.isDec()) {
            nextToken();  // accept Dec token
            String symbolText = current.substring();
            return new Dec128AsExpr(symbolText, current);
        }
        if (current.isKeyword()) {
            if (current.substringEquals(TRUE_VALUE)) {
                LexerToken next = nextToken();  // accept 'true' token
                BoolAsExpr boolAsExpr = new BoolAsExpr(Bool.TRUE, current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseRecOrTupleExpr(boolAsExpr);
                }
                return boolAsExpr;
            }
            if (current.substringEquals(FALSE_VALUE)) {
                LexerToken next = nextToken();  // accept 'false' token
                BoolAsExpr boolAsExpr = new BoolAsExpr(Bool.FALSE, current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseRecOrTupleExpr(boolAsExpr);
                }
                return boolAsExpr;
            }
            if (current.substringEquals(NULL_VALUE)) {
                LexerToken next = nextToken(); // accept 'null' token
                NullAsExpr nullAsExpr = new NullAsExpr(current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseRecOrTupleExpr(nullAsExpr);
                }
                return nullAsExpr;
            }
            if (current.substringEquals(EOF_VALUE)) {
                LexerToken next = nextToken(); // accept 'EOF' token
                EofAsExpr eofAsExpr = new EofAsExpr(current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseRecOrTupleExpr(eofAsExpr);
                }
                return eofAsExpr;
            }
        }
        if (current.isChar()) {
            nextToken(); // accept character token
            char firstCharValue = current.substringCharAt(2); // first char past "&'"
            if (current.length() == 4) {
                // &'a'
                return new CharAsExpr(Char.of(firstCharValue), current);
            }
            if (firstCharValue != '\\') {
                throw new ParserError(INVALID_CHARACTER_LITERAL, current);
            }
            if (current.length() == 5) {
                // &'\n'
                char secondCharValue = current.substringCharAt(2);
                if (secondCharValue == 't') {
                    return new CharAsExpr(Char.of('\t'), current);
                }
                if (secondCharValue == 'b') {
                    return new CharAsExpr(Char.of('\b'), current);
                }
                if (secondCharValue == 'n') {
                    return new CharAsExpr(Char.of('\n'), current);
                }
                if (secondCharValue == 'r') {
                    return new CharAsExpr(Char.of('\r'), current);
                }
                if (secondCharValue == 'f') {
                    return new CharAsExpr(Char.of('\f'), current);
                }
            }
            if (current.length() == 9) {
                // &'\u0000'
                char secondCharValue = current.substringCharAt(2);
                if (secondCharValue == 'u') {
                    String fullTorqChar = current.substring();
                    String unicodeDigits = fullTorqChar.substring(4, fullTorqChar.length() - 1);
                    char charValue = (char) Integer.parseInt(unicodeDigits, 16);
                    return new CharAsExpr(Char.of(charValue), current);
                }
            }
            throw new ParserError(INVALID_CHARACTER_LITERAL, current);
        }
        return null;
    }

    private VarDecl parseVarDecl() {
        LexerToken current = currentToken;
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PATTERN_EXPECTED, current);
        }
        current = currentToken;
        if (current.isOneCharSymbol(UNIFY_OPER_CHAR)) {
            nextToken(); // accept '=' token
            StmtOrExpr expr = parseStmtOrExpr();
            if (expr == null) {
                throw new ParserError(EXPR_EXPECTED, current);
            }
            return new InitVarDecl(pat, expr, pat.adjoin(expr));
        }
        if (pat instanceof IdentAsPat identAsPat) {
            return new IdentVarDecl(identAsPat, identAsPat);
        }
        throw new ParserError(INVALID_VAR_DECL, pat);
    }

    private List<VarDecl> parseVarDecls() {
        List<VarDecl> varDecls = new ArrayList<>();
        VarDecl varDecl = parseVarDecl();
        while (true) {
            varDecls.add(varDecl);
            LexerToken current = currentToken;
            if (!current.isOneCharSymbol(COMMA_CHAR)) {
                break;
            }
            nextToken(); // accept ',' token
            varDecl = parseVarDecl();
        }
        return varDecls;
    }

    private WhileStmt parseWhile() {
        LexerToken whileToken = currentToken;
        nextToken(); // accept 'while' token
        StmtOrExpr cond = parseStmtOrExpr();
        LexerToken current = currentToken;
        if (cond == null) {
            throw new ParserError(EXPR_EXPECTED, current);
        }
        if (!current.isKeyword(DO_VALUE)) {
            throw new ParserError(DO_EXPECTED, current);
        }
        nextToken(); // accept 'do' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new WhileStmt(cond, body, whileToken.adjoin(endToken));
    }

    private ProductOper productOperFor(LexerToken operToken) {
        if (operToken.isOneCharSymbol()) {
            char firstOperChar = operToken.firstChar();
            if (firstOperChar == MULTIPLY_OPER_CHAR) {
                return ProductOper.MULTIPLY;
            }
            if (firstOperChar == DIVIDE_OPER_CHAR) {
                return ProductOper.DIVIDE;
            }
            if (firstOperChar == MODULO_OPER_CHAR) {
                return ProductOper.MODULO;
            }
        }
        return null;
    }

    private RelationalOper relationalOperFor(LexerToken operToken) {
        if (operToken.isOneCharSymbol()) {
            char firstOperChar = operToken.firstChar();
            if (firstOperChar == LESS_THAN_OPER_CHAR) {
                return RelationalOper.LESS_THAN;
            }
            if (firstOperChar == GREATER_THAN_OPER_CHAR) {
                return RelationalOper.GREATER_THAN;
            }
        } else if (operToken.isTwoCharSymbol()) {
            if (operToken.substringEquals(EQUAL_TO_OPER)) {
                return RelationalOper.EQUAL_TO;
            }
            if (operToken.substringEquals(NOT_EQUAL_TO_OPER)) {
                return RelationalOper.NOT_EQUAL_TO;
            }
            if (operToken.substringEquals(LESS_THAN_OR_EQUAL_TO_OPER)) {
                return RelationalOper.LESS_THAN_OR_EQUAL_TO;
            }
            if (operToken.substringEquals(GREATER_THAN_OR_EQUAL_TO_OPER)) {
                return RelationalOper.GREATER_THAN_OR_EQUAL_TO;
            }
        }
        return null;
    }

    private SelectOrApply selectOrApplyFor(SourceSpan operand, LexerToken operToken) {
        if (operToken.isOneCharSymbol()) {
            char firstOperChar = operToken.firstChar();
            if (firstOperChar == DOT_OPER_CHAR) {
                return SelectOrApply.DOT;
            }
            if (firstOperChar == L_BRACKET_CHAR) {
                if (includesLineBreakBetween(operand, operToken)) {
                    return null;
                }
                return SelectOrApply.INDEX;
            }
            if (firstOperChar == L_PAREN_CHAR) {
                if (includesLineBreakBetween(operand, operToken)) {
                    return null;
                }
                return SelectOrApply.APPLY;
            }
        }
        return null;
    }

    public final String source() {
        return lexer.source();
    }

    private SumOper sumOperFor(LexerToken operToken) {
        if (operToken.isOneCharSymbol()) {
            char firstOperChar = operToken.firstChar();
            if (firstOperChar == ADD_OPER_CHAR) {
                return SumOper.ADD;
            }
            if (firstOperChar == SUBTRACT_OPER_CHAR) {
                return SumOper.SUBTRACT;
            }
        }
        return null;
    }

    private Ident tokenToIdent(LexerToken token) {
        if (token.firstChar() != '`') {
            return Ident.create(token.substring());
        }
        int begin = token.sourceBegin() + 1;
        int end = token.sourceEnd() - 1;
        String source = token.source();
        StringBuilder sb = new StringBuilder((end - begin) * 2);
        int i = begin;
        while (i < end) {
            char c1 = source.charAt(i);
            if (c1 == '\\') {
                char c2 = source.charAt(i + 1);
                if (c2 != 'u') {
                    if (c2 == 'r') {
                        c1 = '\r';
                    } else if (c2 == 'n') {
                        c1 = '\n';
                    } else if (c2 == 't') {
                        c1 = '\t';
                    } else if (c2 == 'f') {
                        c1 = '\f';
                    } else if (c2 == 'b') {
                        c1 = '\b';
                    } else if (c2 == '\\') {
                        c1 = '\\';
                    } else if (c2 == '\'') {
                        c1 = '\'';
                    } else {
                        throw new IllegalArgumentException("Invalid escape sequence: " + c1 + c2);
                    }
                    sb.append(c1);
                    i += 2;
                } else {
                    int code = Integer.parseInt("" + source.charAt(i + 2) + source.charAt(i + 3) +
                        source.charAt(i + 4) + source.charAt(i + 5), 16);
                    sb.append(Character.toChars(code));
                    i += 6;
                }
            } else {
                sb.append(c1);
                i++;
            }
        }
        return Ident.create(sb.toString());
    }

    private enum SelectOrApply {
        DOT,
        INDEX,
        APPLY
    }

}
