/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.klvm.*;
import org.torqlang.util.IntegerCounter;
import org.torqlang.util.NeedsImpl;
import org.torqlang.util.SourceSpan;
import org.torqlang.util.SourceString;

import java.util.ArrayList;
import java.util.List;

import static org.torqlang.lang.ParserText.*;
import static org.torqlang.lang.SymbolsAndKeywords.*;
import static org.torqlang.util.ListTools.last;

public final class Parser {

    private final Lexer lexer;

    private LexerToken currentToken;

    public Parser(SourceString source) {
        this.lexer = new Lexer(source);
    }

    public Parser(String source) {
        this.lexer = new Lexer(source);
    }

    public static Lang parse(SourceString source) {
        Parser p = new Parser(source);
        return p.parse();
    }

    public static Lang parse(String source) {
        return parse(SourceString.of(source));
    }

    private static SourceSpan sourceSpanForSeq(List<? extends StmtOrExpr> seq) {
        return seq.get(0).adjoin(last(seq));
    }

    private LexerToken acceptEndToken() {
        LexerToken current = currentToken;
        if (!current.isKeyword(END_VALUE)) {
            throw new ParserError(END_EXPECTED, current);
        }
        nextToken();
        return current;
    }

    private void assertCurrentAtKeyword(String keywordValue, String message) {
        if (!currentToken.isKeyword(keywordValue)) {
            throw new ParserError(message, currentToken);
        }
    }

    private boolean includesLineBreakBetween(SourceSpan first, LexerToken second) {
        SourceString source = source();
        int start = first.sourceEnd() - 1;
        int stop = second.sourceBegin();
        for (int i = start; i < stop && source.containsIndex(i); i++) {
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
            throw new ParserError(EOF_EXPECTED, currentToken);
        }
        return answer;
    }

    private StmtOrExpr parseAccess() {
        StmtOrExpr cast = parseCast();
        if (cast != null) {
            return cast;
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
        IdentAsExpr name = null;
        LexerToken current = nextToken();
        if (current.isIdent()) {
            name = new IdentAsExpr(tokenToIdent(current), current);
            current = nextToken(); // accept IDENT token
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> params = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        current = nextToken(); // accept ')' token
        Protocol protocol = null;
        if (current.isIdent(IMPLEMENTS_VALUE)) {
            nextToken(); // accept 'implements' token
            protocol = parseIntersectionProtocol();
            current = currentToken;
        }
        // In the expression `actor () implements X & Y as PublicProtocol in ... end` the clause
        // `X & Y as PublicProtocol` is equivalent to `protocol PublicProtocol = X & Y` and can be
        // exported along side the actor using the meta tag.
        IdentAsProtocol protocolName = null;
        if (current.isIdent(AS_VALUE)) {
            current = nextToken(); // accept 'as' token
            if (!current.isIdent()) {
                throw new ParserError(IDENT_EXPECTED, current);
            }
            Ident ident = tokenToIdent(current);
            protocolName = IdentAsProtocol.create(ident, current);
            nextToken(); // accept IDENT token
        }
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
        nextToken(); // accept 'in' token
        List<StmtOrExpr> body = new ArrayList<>();
        StmtOrExpr stmtOrExpr = parseActorStmtOrExpr();
        while (stmtOrExpr != null) {
            body.add(stmtOrExpr);
            stmtOrExpr = parseActorStmtOrExpr();
        }
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new ActorStmt(name, params, protocol, protocolName, body, actorToken.adjoin(endToken));
        } else {
            return new ActorExpr(params, protocol, protocolName, body, actorToken.adjoin(endToken));
        }
    }

    private StmtOrExpr parseActorMember() {
        LexerToken current = currentToken;
        if (current.isIdent(HANDLE_VALUE)) {
            LexerToken handleToken = current;
            current = nextToken(); // accept 'handle' token
            if (current.isIdent(ASK_VALUE)) {
                return parseAsk(handleToken);
            } else if (current.isIdent(TELL_VALUE)) {
                return parseTell(handleToken);
            } else if (current.isIdent(STREAM_VALUE)) {
                throw new NeedsImpl("handle stream");
            } else {
                throw new ParserError(ASK_TELL_OR_STREAM_EXPECTED, current);
            }
        } else {
            return parseStmtOrExpr();
        }
    }

    private StmtOrExpr parseActorStmtOrExpr() {
        LexerToken current = currentToken;
        if (current.isIdent(META_VALUE)) {
            LexerToken next = nextToken(); // accept IDENT token
            Ident ident = tokenToIdent(current);
            IdentAsExpr identAsExpr = new IdentAsExpr(ident, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                if (ident.name.equals(META_VALUE)) {
                    MetaStruct metaStruct = parseMetaStruct(identAsExpr);
                    StmtOrExpr stmtOrExpr = parseActorMember();
                    if (stmtOrExpr == null) {
                        throw new ParserError(STMT_OR_EXPR_EXPECTED, currentToken);
                    }
                    stmtOrExpr.setMetaStruct(metaStruct);
                    return stmtOrExpr;
                } else {
                    return parseStructExpr(identAsExpr);
                }
            } else {
                return identAsExpr;
            }
        } else {
            return parseActorMember();
        }
    }

    private StmtOrExpr parseAnd() {
        StmtOrExpr answer = parseRelational();
        if (answer == null) {
            return null;
        }
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
            throw new ParserError(PAT_EXPECTED, current);
        }
        Type responseType = parseResultTypeOptional();
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new AskStmt(pat, body, responseType, handleToken.adjoin(endToken));
    }

    private StmtOrExpr parseAssign() {
        StmtOrExpr left = parseOr();
        if (left == null) {
            return null;
        }
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
        assertCurrentAtKeyword(OF_VALUE, OF_EXPECTED);
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
            throw new ParserError(PAT_EXPECTED, current);
        }
        StmtOrExpr guard = null;
        current = currentToken;
        if (current.isKeyword(WHEN_VALUE)) {
            nextToken(); // accept 'when'
            guard = parseStmtOrExpr();
        }
        assertCurrentAtKeyword(THEN_VALUE, THEN_EXPECTED);
        nextToken(); // accept THEN
        SeqLang body = parseSeq();
        return new CaseClause(pat, guard, body, ofToken.adjoin(body));
    }

    private StmtOrExpr parseCast() {
        StmtOrExpr construct = parseConstruct();
        if (construct != null) {
            return construct;
        }
        LexerToken operToken = currentToken;
        if (!operToken.isTwoCharSymbol(TYPE_OPER)) {
            return null;
        }
        nextToken(); // accept '::' token
        StmtOrExpr right = parseCast();
        if (right == null) {
            throw new ParserError(EXPR_EXPECTED, currentToken);
        }
        throw new NeedsImpl();
        // TODO:
        //    return new CastExpr(right, operToken.adjoin(right));
    }

    private CharAsExpr parseChar() {
        LexerToken current = currentToken;
        nextToken(); // accept character token
        char firstCharValue = current.substringCharAt(2); // first char past "&'"
        if (current.length() == 4) {
            // &'a'
            return new CharAsExpr(Char.of(firstCharValue), current);
        }
        if (firstCharValue != '\\') {
            throw new ParserError(CHAR_EXPECTED, current);
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
        throw new ParserError(CHAR_EXPECTED, current);
    }

    private StmtOrExpr parseConstruct() {
        StmtOrExpr valueOrIdentAsExpr = parseValueOrIdentAsExpr();
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
            int keywordLen = current.length();
            if (keywordLen == 2) {
                if (current.substringEquals(IF_VALUE)) {
                    return parseIf();
                }
            } else if (keywordLen == 3) {
                if (current.substringEquals(NEW_VALUE)) {
                    return parseNew();
                }
                if (current.substringEquals(VAR_VALUE)) {
                    nextToken(); // accept 'var' token
                    List<VarDecl> varDecls = parseVarDecls();
                    return new VarStmt(varDecls, current.adjoin(last(varDecls)));
                }
                if (current.substringEquals(FOR_VALUE)) {
                    return parseFor();
                }
                if (current.substringEquals(ACT_VALUE)) {
                    return parseAct();
                }
                if (current.substringEquals(TRY_VALUE)) {
                    return parseTry();
                }
            } else if (keywordLen == 4) {
                if (current.substringEquals(CASE_VALUE)) {
                    return parseCase();
                }
                if (current.substringEquals(FUNC_VALUE)) {
                    return parseFunc();
                }
                if (current.substringEquals(PROC_VALUE)) {
                    return parseProc();
                }
                if (current.substringEquals(TYPE_VALUE)) {
                    return parseType();
                }
                if (current.substringEquals(SELF_VALUE)) {
                    nextToken(); // accept 'self' token
                    return new IdentAsExpr(Ident.$SELF, current);
                }
                if (current.substringEquals(SKIP_VALUE)) {
                    nextToken(); // accept 'skip' token
                    return new SkipStmt(current);
                }
            } else if (keywordLen == 5) {
                if (current.substringEquals(WHILE_VALUE)) {
                    return parseWhile();
                }
                if (current.substringEquals(SPAWN_VALUE)) {
                    return parseSpawn();
                }
                if (current.substringEquals(ACTOR_VALUE)) {
                    return parseActor();
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
                if (current.substringEquals(BREAK_VALUE)) {
                    nextToken(); // accept 'break' token
                    return new BreakStmt(current);
                }
            } else if (keywordLen == 6) {
                if (current.substringEquals(IMPORT_VALUE)) {
                    return parseImport();
                }
                if (current.substringEquals(RETURN_VALUE)) {
                    return parseReturn();
                }
            } else {
                if (current.substringEquals(PROTOCOL_VALUE)) {
                    return parseProtocol();
                }
                if (current.substringEquals(CONTINUE_VALUE)) {
                    nextToken(); // accept 'continue' token
                    return new ContinueStmt(current);
                }
            }
        }
        return null;
    }

    private FieldExpr parseFieldExpr(IntegerCounter nextImpliedFeature) {
        StmtOrExpr featureOrValueExpr = parseStmtOrExpr();
        if (featureOrValueExpr == null) {
            return null;
        }
        FeatureExpr featureExpr;
        StmtOrExpr valueExpr;
        LexerToken current = currentToken;
        if (current.isOneCharSymbol(COLON_CHAR)) {
            current = nextToken(); // accept ':'
            if (!(featureOrValueExpr instanceof FeatureExpr featureExprParsed)) {
                throw new ParserError(FEATURE_EXPR_EXPECTED, current);
            }
            featureExpr = featureExprParsed;
            valueExpr = parseStmtOrExpr();
            if (valueExpr == null) {
                throw new ParserError(VALUE_EXPR_EXPECTED, current);
            }
        } else {
            int nextFeature = nextImpliedFeature.getAndAdd(1);
            featureExpr = new Int64AsExpr(Int32.of(nextFeature), featureOrValueExpr.toSourceBegin());
            valueExpr = featureOrValueExpr;
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
                throw new ParserError(FEATURE_PAT_EXPECTED, featureOrValuePat);
            }
            featurePat = featurePatParsed;
            valuePat = parsePat();
            if (valuePat == null) {
                throw new ParserError(VALUE_PAT_EXPECTED, current);
            }
        } else {
            int nextFeature = nextImpliedFeature.getAndAdd(1);
            featurePat = new Int64AsPat(Int32.of(nextFeature), featureOrValuePat.toSourceBegin());
            valuePat = featureOrValuePat;
        }
        return new FieldPat(featurePat, valuePat, featurePat.adjoin(valuePat));
    }

    private FieldType parseFieldType(IntegerCounter nextImpliedFeature) {
        Type featureOrValueType = parseTypeExpr();
        if (featureOrValueType == null) {
            return null;
        }
        FeatureAsType featureAsType;
        Type valueType;
        if (featureOrValueType instanceof MethodType methodType) {
            IdentAsExpr name = methodType.name();
            if (name == null) {
                throw new ParserError(FUNC_NAME_REQUIRED, methodType);
            }
            featureAsType = new StrAsType(Str.of(name.ident.name), name);
            valueType = methodType;
        } else {
            LexerToken current = currentToken;
            if (current.isOneCharSymbol(COLON_CHAR)) {
                current = nextToken(); // accept ':' token
                if (!(featureOrValueType instanceof FeatureAsType featureAsTypeFound)) {
                    throw new ParserError(FEATURE_TYPE_EXPECTED, current);
                }
                featureAsType = featureAsTypeFound;
                valueType = parseTypeExpr();
                if (valueType == null) {
                    throw new ParserError(VALUE_TYPE_EXPECTED, current);
                }
            } else {
                int nextFeature = nextImpliedFeature.getAndAdd(1);
                featureAsType = new Int32AsType(Int32.of(nextFeature), featureOrValueType.toSourceBegin());
                valueType = featureOrValueType;
            }
        }
        return new FieldType(featureAsType, valueType, featureAsType.adjoin(valueType));
    }

    private List<FieldType> parseFieldTypes() {
        IntegerCounter nextImpliedFeature = new IntegerCounter(0);
        List<FieldType> fieldTypes = new ArrayList<>();
        FieldType fieldType = parseFieldType(nextImpliedFeature);
        LexerToken current = currentToken;
        if (fieldType != null) {
            fieldTypes.add(fieldType);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                fieldType = parseFieldType(nextImpliedFeature);
                current = currentToken;
                if (fieldType != null) {
                    fieldTypes.add(fieldType);
                } else {
                    break;
                }
            }
        }
        return fieldTypes;
    }

    private ForStmt parseFor() {
        LexerToken forToken = currentToken;
        LexerToken current = nextToken(); // accept 'for' token
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PAT_EXPECTED, current);
        }
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
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
        IdentAsExpr name = null;
        LexerToken current = nextToken(); // accept 'func' token
        if (current.isIdent()) {
            name = new IdentAsExpr(tokenToIdent(current), current);
            current = nextToken(); // accept IDENT token
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> params = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        Type returnType = parseResultTypeOptional();
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new FuncStmt(name, params, returnType, body, funcToken.adjoin(endToken));
        } else {
            return new FuncExpr(params, returnType, body, funcToken.adjoin(endToken));
        }
    }

    private FuncType parseFuncType() {
        LexerToken funcToken = currentToken;
        LexerToken current = nextToken(); // accept 'func' token
        IdentAsExpr name = null;
        if (current.isIdent()) {
            name = new IdentAsExpr(tokenToIdent(current), current);
            current = nextToken(); // accept IDENT token
        }
        List<TypeParam> typeParams;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            typeParams = parseTypeParamList();
            current = currentToken;
        } else {
            typeParams = List.of();
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> params = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        Type returnType = parseResultTypeRequired();
        return new FuncType(name, typeParams, params, returnType, funcToken.adjoin(returnType));
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
        SourceSpan groupSpan = groupBegin.adjoin(groupEnd);
        if (seq.list.size() == 1) {
            return new GroupExpr(seq.list.get(0), groupSpan);
        } else {
            return new GroupExpr(seq, groupSpan);
        }
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
        assertCurrentAtKeyword(THEN_VALUE, THEN_EXPECTED);
        nextToken(); // accept THEN token
        SeqLang body = parseSeq();
        return new IfClause(condition, body, ifOrElseIfToken.adjoin(body));
    }

    private ImportStmt parseImport() {
        LexerToken importToken = currentToken;
        List<IdentAsExpr> path = new ArrayList<>();
        List<ImportName> names = new ArrayList<>();
        LexerToken current = nextToken(); // accept 'import' token
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, current);
        }
        path.add(new IdentAsExpr(tokenToIdent(current), current));
        current = nextToken(); // accept IDENT token
        while (current.isOneCharSymbol(DOT_OPER_CHAR)) {
            current = nextToken(); // accept '.' token
            if (current.isIdent()) {
                path.add(new IdentAsExpr(tokenToIdent(current), current));
            } else if (current.isOneCharSymbol(L_BRACE_CHAR)) {
                break;
            } else {
                throw new ParserError(IDENT_EXPECTED, current);
            }
            current = nextToken(); // accept IDENT token
        }
        List<IdentAsExpr> qualifier;
        // Current token is either the '{' or one token past a name
        if (current.isOneCharSymbol(L_BRACE_CHAR)) {
            qualifier = path;
            current = nextToken(); // accept '{' token
            while (current.isIdent()) {
                IdentAsExpr name = new IdentAsExpr(tokenToIdent(current), current);
                current = nextToken(); // accept Ident token
                if (current.isIdent(AS_VALUE)) {
                    current = nextToken(); // accept 'as' token
                    if (!current.isIdent()) {
                        throw new ParserError(IDENT_EXPECTED, current);
                    }
                    IdentAsExpr alias = new IdentAsExpr(tokenToIdent(current), current);
                    current = nextToken(); // accept alias IDENT token
                    names.add(new ImportName(name, alias, current));
                } else {
                    names.add(new ImportName(name, current));
                }
                if (current.isOneCharSymbol(COMMA_CHAR)) {
                    current = nextToken(); // accept ',' token
                }
            }
            if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
                throw new ParserError(R_BRACE_EXPECTED, current);
            }
            nextToken(); // accept '}' token
        } else {
            qualifier = path.subList(0, path.size() - 1);
            IdentAsExpr name = last(path);
            names.add(new ImportName(name, name));
        }
        return new ImportStmt(qualifier, names, importToken.adjoin(last(names)));
    }

    private Protocol parseIntersectionProtocol() {
        Protocol answer = parseProtocolExpr();
        if (answer == null) {
            return null;
        }
        while (currentToken.isOneCharSymbol(AND_OPER_CHAR)) {
            nextToken(); // accept '&' token
            Protocol right = parseProtocolExpr();
            if (right == null) {
                throw new ParserError(PROTOCOL_EXPECTED, currentToken);
            }
            answer = new IntersectionProtocol(answer, right, answer.adjoin(right));
        }
        return answer;
    }

    private Type parseIntersectionType() {
        Type answer = parseTypeTerm();
        if (answer == null) {
            return null;
        }
        while (currentToken.isOneCharSymbol(AND_OPER_CHAR)) {
            nextToken(); // accept '&' token
            Type right = parseTypeTerm();
            if (right == null) {
                throw new ParserError(TYPE_EXPECTED, currentToken);
            }
            answer = new IntersectionType(answer, right, answer.adjoin(right));
        }
        return answer;
    }

    private LocalLang parseLocal() {
        LexerToken localToken = currentToken;
        nextToken(); // accept 'local' token
        List<VarDecl> varDecls = parseVarDecls();
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        return new LocalLang(varDecls, body, localToken.adjoin(endToken));
    }

    private MetaField parseMetaField(IntegerCounter nextImpliedFeature) {
        MetaValue featureOrValue = parseMetaValue();
        if (featureOrValue == null) {
            return null;
        }
        MetaFeature feature;
        MetaValue value;
        LexerToken current = currentToken;
        if (current.isOneCharSymbol(COLON_CHAR)) {
            current = nextToken(); // accept ':' token
            if (!(featureOrValue instanceof MetaFeature featureParsed)) {
                throw new ParserError(META_FEATURE_EXPECTED, current);
            }
            feature = featureParsed;
            value = parseMetaValue();
            if (value == null) {
                throw new ParserError(META_VALUE_EXPECTED, current);
            }
        } else {
            int nextFeature = nextImpliedFeature.getAndAdd(1);
            feature = new Int64AsExpr(Int32.of(nextFeature), featureOrValue.toSourceBegin());
            value = featureOrValue;
        }
        return new MetaField(feature, value, feature.adjoin(value));
    }

    /*
     * Note that parsing meta numbers differs from parsing number expressions. Meta numbers are simply unsigned or
     * negative signed literals. In contrast, negative numer expressions are unary expressions such that `--1` is a
     * valid number expression but not a valid meta number.
     */
    private MetaValue parseMetaNum(LexerToken current) {
        boolean negative = false;
        if (current.isOneCharSymbol(SUBTRACT_OPER_CHAR)) {
            negative = true;
            current = nextToken();
        }
        if (current.isInt()) {
            nextToken(); // accept Int token
            String symbolText = negative ? "-" + current.substring() : current.substring();
            return new Int64AsExpr(symbolText, current);
        }
        if (current.isFlt()) {
            nextToken();  // accept Flt token
            String symbolText = negative ? "-" + current.substring() : current.substring();
            return new Flt64AsExpr(symbolText, current);
        }
        if (current.isDec()) {
            nextToken();  // accept Dec token
            String symbolText = negative ? "-" + current.substring() : current.substring();
            return new Dec128AsExpr(symbolText, current);
        }
        if (negative) {
            throw new ParserError(ParserText.NUMBER_EXPECTED, current);
        } else {
            return null;
        }
    }

    private MetaRec parseMetaRec(LabelExpr label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '{' token
        IntegerCounter nextImpliedFeature = new IntegerCounter(0);
        List<MetaField> fields = new ArrayList<>();
        MetaField field = parseMetaField(nextImpliedFeature);
        LexerToken current = currentToken;
        if (field != null) {
            fields.add(field);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                field = parseMetaField(nextImpliedFeature);
                current = currentToken;
                if (field != null) {
                    fields.add(field);
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new MetaRec(fields, recSpan);
    }

    private MetaStruct parseMetaStruct(LabelExpr label) {
        LexerToken current = nextToken(); // accept '#' token
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseMetaRec(label);
            } else if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseMetaTuple(label);
            }
        }
        throw new ParserError(L_BRACE_OR_L_BRACKET_EXPECTED, current);
    }

    private StmtOrExpr parseMetaStructAndNextStmtOrExpr(LabelExpr label) {
        MetaStruct metaStruct = parseMetaStruct(label);
        StmtOrExpr stmtOrExpr = parseStmtOrExpr();
        if (stmtOrExpr == null) {
            throw new ParserError(STMT_OR_EXPR_EXPECTED, currentToken);
        }
        stmtOrExpr.setMetaStruct(metaStruct);
        return stmtOrExpr;
    }

    private MetaTuple parseMetaTuple(LabelExpr label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '[' token
        List<MetaValue> values = new ArrayList<>();
        MetaValue value = parseMetaValue();
        LexerToken current = currentToken;
        if (value != null) {
            values.add(value);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                value = parseMetaValue();
                current = currentToken;
                if (value != null) {
                    values.add(value);
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new MetaTuple(values, recSpan);
    }

    private MetaValue parseMetaValue() {
        LexerToken current = currentToken;
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseMetaRec(null);
            }
            if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseMetaTuple(null);
            }
            if (current.firstCharEquals(SUBTRACT_OPER_CHAR)) {
                return parseMetaNum(current);
            }
            return null;
        }
        if (current.isStr()) {
            nextToken(); // accept STR token
            String substring = Str.unquote(current.source(), current.sourceBegin(), current.sourceEnd());
            return new StrAsExpr(Str.of(substring), current);
        }
        if (current.isIdent()) {
            LexerToken next = nextToken(); // accept IDENT token
            Ident ident = tokenToIdent(current);
            IdentAsExpr identAsExpr = new IdentAsExpr(ident, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                if (ident.name.equals(META_VALUE)) {
                    return parseMetaStruct(identAsExpr);
                } else {
                    throw new ParserError(META_VALUE_EXPECTED, current);
                }
            } else {
                throw new ParserError(META_VALUE_EXPECTED, current);
            }
        }
        MetaValue metaNum = parseMetaNum(current);
        if (metaNum != null) {
            return metaNum;
        }
        if (current.isKeyword()) {
            if (current.substringEquals(TRUE_VALUE)) {
                nextToken();  // accept 'true' token
                return new BoolAsExpr(Bool.TRUE, current);
            }
            if (current.substringEquals(FALSE_VALUE)) {
                nextToken();  // accept 'false' token
                return new BoolAsExpr(Bool.FALSE, current);
            }
            if (current.substringEquals(NULL_VALUE)) {
                nextToken(); // accept 'null' token
                return new NullAsExpr(current);
            }
            if (current.substringEquals(EOF_VALUE)) {
                nextToken(); // accept 'EOF' token
                return new EofAsExpr(current);
            }
        }
        if (current.isChar()) {
            return parseChar();
        }
        return null;
    }

    public final ModuleStmt parseModule() {
        LexerToken current = nextToken(); // load first token
        if (!current.isKeyword(PACKAGE_VALUE)) {
            throw new ParserError(PACKAGE_EXPECTED, currentToken);
        }
        PackageStmt packageStmt = parsePackage();
        while (currentToken.isOneCharSymbol(SEMICOLON_CHAR)) {
            nextToken();
        }
        List<StmtOrExpr> body = new ArrayList<>();
        StmtOrExpr next = parseStmtOrExpr();
        while (next != null) {
            body.add(next);
            next = parseStmtOrExpr();
        }
        if (!currentToken.isEof()) {
            throw new ParserError(EOF_EXPECTED, currentToken);
        }
        return new ModuleStmt(packageStmt, body, packageStmt.adjoin(last(body)));
    }

    private NewExpr parseNew() {
        LexerToken newToken = currentToken;
        LexerToken current = nextToken(); // accept 'new' operator
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, currentToken);
        }
        TypeApply typeApply = parseTypeApply();
        current = currentToken;
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<StmtOrExpr> args = parseArgList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, currentToken);
        }
        LexerToken endNewToken = current;
        nextToken(); // accept ')' token
        return new NewExpr(typeApply, args, newToken.adjoin(endNewToken));
    }

    private StmtOrExpr parseOr() {
        StmtOrExpr answer = parseAnd();
        if (answer == null) {
            return null;
        }
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

    private PackageStmt parsePackage() {
        LexerToken packageToken = currentToken;
        LexerToken current = nextToken(); // accept 'package' token
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, current);
        }
        List<IdentAsExpr> path = new ArrayList<>();
        IdentAsExpr elem = new IdentAsExpr(tokenToIdent(current), current);
        path.add(elem);
        current = nextToken(); // accept IDENT token
        while (current.isOneCharSymbol(DOT_OPER_CHAR)) {
            current = nextToken(); // accept '.' token
            if (!current.isIdent()) {
                throw new ParserError(IDENT_EXPECTED, current);
            }
            elem = new IdentAsExpr(tokenToIdent(current), current);
            path.add(elem);
            current = nextToken(); // accept IDENT token
        }
        // Current token is now one past the import expression
        return new PackageStmt(path, packageToken.adjoin(last(path)));
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
                LexerToken next = nextToken(); // accept '~' token
                if (!next.isIdent()) {
                    throw new ParserError(IDENT_EXPECTED, next);
                }
                Ident ident = tokenToIdent(next);
                IdentAsPat identAsPat = new IdentAsPat(ident, true, current.adjoin(next));
                next = nextToken(); // accept IDENT token
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseStructPat(identAsPat);
                }
                return identAsPat;
            }
            return null;
        }
        if (current.isStr()) {
            LexerToken next = nextToken(); // accept Str token
            String substring = Str.unquote(current.source(), current.sourceBegin(), current.sourceEnd());
            StrAsPat strAsPat = new StrAsPat(Str.of(substring), current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructPat(strAsPat);
            }
            return strAsPat;
        }
        if (current.isIdent()) {
            LexerToken next = nextToken(); // accept Ident token
            Ident ident = tokenToIdent(current);
            if (!next.isTwoCharSymbol(TYPE_OPER)) {
                return new IdentAsPat(ident, false, current);
            }
            nextToken(); // accept '::' token
            Type type = parseTypeExpr();
            return new IdentAsPat(ident, false, type, current.adjoin(type));
        }
        if (current.isInt()) {
            nextToken(); // accept Int token
            String symbolText = current.substring();
            return new Int64AsPat(symbolText, current);
        }
        if (current.substringEquals(TRUE_VALUE)) {
            LexerToken next = nextToken();  // accept 'true' token
            BoolAsPat boolAsPat = new BoolAsPat(Bool.TRUE, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructPat(boolAsPat);
            }
            return boolAsPat;
        }
        if (current.substringEquals(FALSE_VALUE)) {
            LexerToken next = nextToken();  // accept 'false' token
            BoolAsPat boolAsPat = new BoolAsPat(Bool.FALSE, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructPat(boolAsPat);
            }
            return boolAsPat;
        }
        if (current.substringEquals(NULL_VALUE)) {
            LexerToken next = nextToken(); // accept 'null' token
            NullAsPat nullAsPat = new NullAsPat(current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructPat(nullAsPat);
            }
            return nullAsPat;
        }
        if (current.substringEquals(EOF_VALUE)) {
            LexerToken next = nextToken(); // accept 'eof' token
            EofAsPat eofAsPat = new EofAsPat(current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructPat(eofAsPat);
            }
            return eofAsPat;
        }
        return null;
    }

    private List<Pat> parsePatList() {
        List<Pat> pats = new ArrayList<>();
        Pat pat = parsePat();
        while (pat != null) {
            pats.add(pat);
            LexerToken current = currentToken;
            if (!current.isOneCharSymbol(COMMA_CHAR)) {
                break;
            }
            nextToken(); // accept ',' token
            pat = parsePat();
        }
        return pats;
    }

    private ProcLang parseProc() {
        LexerToken procToken = currentToken;
        IdentAsExpr name = null;
        LexerToken current = nextToken(); // accept 'proc' token
        if (current.isIdent()) {
            name = new IdentAsExpr(tokenToIdent(current), current);
            current = nextToken(); // accept IDENT token
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> params = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
        nextToken(); // accept 'in' token
        SeqLang body = parseSeq();
        LexerToken endToken = acceptEndToken();
        if (name != null) {
            return new ProcStmt(name, params, body, procToken.adjoin(endToken));
        } else {
            return new ProcExpr(params, body, procToken.adjoin(endToken));
        }
    }

    private ProcType parseProcType() {
        LexerToken funcToken = currentToken;
        LexerToken current = nextToken(); // accept 'proc' token
        IdentAsExpr name = null;
        if (current.isIdent()) {
            name = new IdentAsExpr(tokenToIdent(current), current);
            current = nextToken(); // accept IDENT token
        }
        List<TypeParam> typeParams;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            typeParams = parseTypeParamList();
            current = currentToken;
        } else {
            typeParams = List.of();
        }
        if (!current.isOneCharSymbol(L_PAREN_CHAR)) {
            throw new ParserError(L_PAREN_EXPECTED, current);
        }
        nextToken(); // accept '(' token
        List<Pat> params = parsePatList();
        current = currentToken;
        if (!current.isOneCharSymbol(R_PAREN_CHAR)) {
            throw new ParserError(R_PAREN_EXPECTED, current);
        }
        nextToken(); // accept ')' token
        return new ProcType(name, typeParams, params, funcToken.adjoin(current));
    }

    private StmtOrExpr parseProduct() {
        StmtOrExpr answer = parseUnary();
        if (answer == null) {
            return null;
        }
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

    private ProtocolStmt parseProtocol() {
        LexerToken typeToken = currentToken;
        LexerToken current = nextToken(); // accept 'protocol' token
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, current);
        }
        IdentAsExpr name = new IdentAsExpr(tokenToIdent(current), current);
        current = nextToken(); // accept IDENT token
        List<TypeParam> typeParams;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            typeParams = parseTypeParamList();
            current = currentToken;
        } else {
            typeParams = List.of();
        }
        if (!current.isOneCharSymbol(EQUAL_OPER_CHAR)) {
            throw new ParserError(EQUAL_EXPECTED, current);
        }
        nextToken(); // accept '=' token
        Protocol body = parseIntersectionProtocol();
        return new ProtocolStmt(name, typeParams, body, typeToken.adjoin(body));
    }

    private ProtocolApply parseProtocolApply(LexerToken identToken) {
        Ident ident = tokenToIdent(identToken);
        IdentAsProtocol name = IdentAsProtocol.create(ident, currentToken);
        LexerToken current = currentToken;
        List<Type> typeArgs;
        LexerToken endProtocolApply = identToken;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            nextToken(); // accept '[' token
            typeArgs = parseTypeArgList();
            current = currentToken;
            if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
                throw new ParserError(R_BRACKET_EXPECTED, current);
            }
            endProtocolApply = current;
            nextToken(); // accept ']' token
        } else {
            typeArgs = List.of();
        }
        return new ProtocolApply(name, typeArgs, identToken.adjoin(endProtocolApply));
    }

    private Protocol parseProtocolExpr() {
        LexerToken current = currentToken;
        if (current.isIdent()) {
            LexerToken identToken = current;
            Ident name = tokenToIdent(current);
            IdentAsProtocol identAsProtocol = IdentAsProtocol.create(name, identToken);
            current = nextToken(); // accept IDENT token
            if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
                return parseProtocolApply(identToken);
            } else {
                return identAsProtocol;
            }
        } else if (current.isOneCharSymbol(L_BRACE_CHAR)) {
            return parseProtocolStruct();
        }
        return null;
    }

    private ProtocolHandler parseProtocolHandler() {
        LexerToken current = currentToken;
        if (current.isIdent(ASK_VALUE)) {
            nextToken(); // accept 'ask' token
            Pat pat = parsePat();
            if (pat == null) {
                throw new ParserError(PAT_EXPECTED, current);
            }
            Type responseType = parseResultTypeRequired();
            SourceSpan askSpan = responseType == null ? current.adjoin(pat) : current.adjoin(responseType);
            return new ProtocolAskHandler(pat, responseType, askSpan);
        } else if (current.isIdent(TELL_VALUE)) {
            nextToken(); // accept 'tell' token
            Pat pat = parsePat();
            return new ProtocolTellHandler(pat, current.adjoin(pat));
        } else if (current.isIdent(STREAM_VALUE)) {
            nextToken(); // accept 'stream' token
            Pat pat = parsePat();
            if (pat == null) {
                throw new ParserError(PAT_EXPECTED, current);
            }
            Type responseType = parseResultTypeRequired();
            SourceSpan askSpan = responseType == null ? current.adjoin(pat) : current.adjoin(responseType);
            return new ProtocolStreamHandler(pat, responseType, askSpan);
        } else {
            return null;
        }
    }

    private ProtocolStruct parseProtocolStruct() {
        LexerToken structToken = currentToken;
        nextToken(); // accept '{' token
        List<ProtocolHandler> handlers = new ArrayList<>();
        ProtocolHandler handler = parseProtocolHandler();
        LexerToken current = currentToken;
        if (handler != null) {
            handlers.add(handler);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                handler = parseProtocolHandler();
                current = currentToken;
                if (handler != null) {
                    handlers.add(handler);
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(ASK_TELL_OR_STREAM_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan structSpan = structToken.adjoin(current);
        return new ProtocolStruct(handlers, structSpan);
    }

    private RecExpr parseRecExpr(Expr label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '{' token
        IntegerCounter nextImpliedFeature = new IntegerCounter(0);
        List<FieldExpr> fieldExprs = new ArrayList<>();
        FieldExpr fieldExpr = parseFieldExpr(nextImpliedFeature);
        LexerToken current = currentToken;
        if (fieldExpr != null) {
            fieldExprs.add(fieldExpr);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                fieldExpr = parseFieldExpr(nextImpliedFeature);
                current = currentToken;
                if (fieldExpr != null) {
                    fieldExprs.add(fieldExpr);
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new RecExpr(label, fieldExprs, recSpan);
    }

    private RecPat parseRecPat(LabelPat label) {
        LexerToken recToken = currentToken;
        nextToken(); // accept '{' token
        IntegerCounter nextImpliedFeature = new IntegerCounter(0);
        boolean partialArity = false;
        List<FieldPat> fieldPats = new ArrayList<>();
        FieldPat fieldPat = parseFieldPat(nextImpliedFeature);
        LexerToken current = currentToken;
        if (fieldPat != null) {
            fieldPats.add(fieldPat);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                fieldPat = parseFieldPat(nextImpliedFeature);
                current = currentToken;
                if (fieldPat != null) {
                    fieldPats.add(fieldPat);
                } else if (current.isThreeCharSymbol(ARITY_OPER)) {
                    current = nextToken(); // accept '...' token
                    partialArity = true;
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new RecPat(label, fieldPats, partialArity, recSpan);
    }

    private Type parseRecTypeExpr(LabelAsType label) {
        LexerToken recToken = currentToken;
        LexerToken current = nextToken(); // accept '{' token
        List<FieldType> staticFields = null;
        if (current.isIdent(STATIC_VALUE)) {
            staticFields = parseStaticFields();
        }
        List<FieldType> fields = parseFieldTypes();
        current = currentToken;
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        SourceSpan recSpan = label == null ? recToken.adjoin(current) : label.adjoin(current);
        return new RecTypeExpr(label, staticFields, fields, recSpan);
    }

    private StmtOrExpr parseRelational() {
        StmtOrExpr answer = parseSum();
        if (answer == null) {
            return null;
        }
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

    private Type parseResultTypeOptional() {
        LexerToken current = currentToken;
        Type type = null;
        if (current.isTwoCharSymbol(RESULT_TYPE_OPER)) {
            nextToken(); // accept '->' token
            type = parseTypeExpr();
        }
        return type;
    }

    private Type parseResultTypeRequired() {
        LexerToken current = currentToken;
        if (!current.isTwoCharSymbol(RESULT_TYPE_OPER)) {
            throw new ParserError(ARROW_EXPECTED, current);
        }
        nextToken(); // accept '->' token
        return parseTypeExpr();
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

    private List<FieldType> parseStaticFields() {
        LexerToken current = nextToken(); // accept 'static' token
        if (!current.isOneCharSymbol(L_BRACE_CHAR)) {
            throw new ParserError(L_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '{' token
        List<FieldType> fieldTypes = parseFieldTypes();
        current = currentToken;
        if (!current.isOneCharSymbol(R_BRACE_CHAR)) {
            throw new ParserError(R_BRACE_EXPECTED, current);
        }
        nextToken(); // accept '}' token
        return fieldTypes;
    }

    private Expr parseStructExpr(LabelExpr label) {
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

    private Pat parseStructPat(LabelPat label) {
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

    private Type parseStructType(LabelAsType label) {
        LexerToken current = nextToken(); // accept '#' token
        if (current.isOneCharSymbol()) {
            if (current.firstCharEquals(L_BRACE_CHAR)) {
                return parseRecTypeExpr(label);
            } else if (current.firstCharEquals(L_BRACKET_CHAR)) {
                return parseTupleTypeExpr(label);
            }
        }
        throw new ParserError(L_BRACE_OR_L_BRACKET_EXPECTED, current);
    }

    private StmtOrExpr parseSum() {
        StmtOrExpr answer = parseProduct();
        if (answer == null) {
            return null;
        }
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
            throw new ParserError(PAT_EXPECTED, current);
        }
        assertCurrentAtKeyword(IN_VALUE, IN_EXPECTED);
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
                throw new ParserError(PAT_EXPECTED, current);
            }
            assertCurrentAtKeyword(THEN_VALUE, THEN_EXPECTED);
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
        LexerToken tupleToken = currentToken;
        nextToken(); // accept '[' token
        List<StmtOrExpr> valueExprs = new ArrayList<>();
        StmtOrExpr value = parseStmtOrExpr();
        LexerToken current = currentToken;
        if (value != null) {
            valueExprs.add(value);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                value = parseStmtOrExpr();
                current = currentToken;
                if (value != null) {
                    valueExprs.add(value);
                } else {
                    break;
                }
            }
        }
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
        LexerToken current = currentToken;
        if (pat != null) {
            valuePats.add(pat);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                pat = parsePat();
                current = currentToken;
                if (pat != null) {
                    valuePats.add(pat);
                } else if (current.isThreeCharSymbol(ARITY_OPER)) {
                    current = nextToken(); // accept '...' token
                    partialArity = true;
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        SourceSpan tupleSpan = label == null ? tupleToken.adjoin(current) : label.adjoin(current);
        return new TuplePat(label, valuePats, partialArity, tupleSpan);
    }

    private Type parseTupleTypeExpr(LabelAsType label) {
        LexerToken tupleToken = currentToken;
        LexerToken current = nextToken(); // accept '[' token
        List<FieldType> staticFields = null;
        if (current.isIdent(STATIC_VALUE)) {
            staticFields = parseStaticFields();
        }
        List<Type> values = new ArrayList<>();
        Type value = parseTypeExpr();
        current = currentToken;
        if (value != null) {
            values.add(value);
            while (current.isOneCharSymbol(COMMA_CHAR)) {
                nextToken(); // accept ',' token
                value = parseTypeExpr();
                current = currentToken;
                if (value != null) {
                    values.add(value);
                } else {
                    break;
                }
            }
        }
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        SourceSpan tupleSpan = label == null ? tupleToken.adjoin(current) : label.adjoin(current);
        return new TupleTypeExpr(label, staticFields, values, tupleSpan);
    }

    private TypeStmt parseType() {
        LexerToken typeToken = currentToken;
        LexerToken current = nextToken(); // accept 'type' token
        if (!current.isIdent()) {
            throw new ParserError(IDENT_EXPECTED, current);
        }
        TypeDecl typeDecl = parseTypeDecl();
        current = currentToken;
        if (!current.isOneCharSymbol(EQUAL_OPER_CHAR)) {
            throw new ParserError(EQUAL_EXPECTED, current);
        }
        nextToken(); // accept '=' token
        Type body = parseTypeExpr();
        return new TypeStmt(typeDecl, body, typeToken.adjoin(body));
    }

    /*
     * Type application occurs in two contexts:
     *     - Direct type construction using the new operator, such as `var x = new Array[Int32]`
     *     - Indirect type construction when the type constructor is used through a type alias, such
     *       as `MyType = Array[Int32]`.
     */
    private TypeApply parseTypeApply() {
        LexerToken identToken = currentToken;
        nextToken(); // accept IDENT token
        return parseTypeApply(identToken);
    }

    private TypeApply parseTypeApply(LexerToken identToken) {
        Ident ident = tokenToIdent(identToken);
        IdentAsType name = IdentAsType.create(ident, currentToken);
        LexerToken current = currentToken;
        List<Type> typeArgs;
        LexerToken endTypeApplyToken = identToken;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            nextToken(); // accept '[' token
            typeArgs = parseTypeArgList();
            current = currentToken;
            if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
                throw new ParserError(R_BRACKET_EXPECTED, current);
            }
            endTypeApplyToken = current;
            nextToken(); // accept ']' token
        } else {
            typeArgs = List.of();
        }
        return new TypeApply(name, typeArgs, identToken.adjoin(endTypeApplyToken));
    }

    private List<Type> parseTypeArgList() {
        List<Type> args = new ArrayList<>();
        Type arg = parseTypeExpr();
        while (arg != null) {
            args.add(arg);
            LexerToken current = currentToken;
            if (!current.isOneCharSymbol(COMMA_CHAR)) {
                break;
            }
            nextToken(); // accept ',' token
            arg = parseTypeExpr();
        }
        return args;
    }

    private TypeDecl parseTypeDecl() {
        LexerToken current = currentToken;
        IdentAsType name = IdentAsType.create(tokenToIdent(current), current);
        current = nextToken(); // accept IDENT token
        List<TypeParam> typeParams;
        if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            typeParams = parseTypeParamList();
        } else {
            typeParams = List.of();
        }
        SourceSpan typeDeclSpan = name;
        if (!typeParams.isEmpty()) {
            typeDeclSpan = name.adjoin(last(typeParams));
        }
        return new TypeDecl(name, typeParams, typeDeclSpan);
    }

    private Type parseTypeExpr() {
        LexerToken current = currentToken;
        if (current.isIdent(Type.ACTOR_REF)) {
            // TODO
            throw new NeedsImpl();
        } else if (current.isIdent(Type.ACTOR_CFG)) {
            // TODO
            throw new NeedsImpl();
        } else if (current.isIdent(Type.ACTOR_CTOR)) {
            // TODO
            throw new NeedsImpl();
        } else {
            return parseUnionType();
        }
    }

    private List<TypeParam> parseTypeParamList() {
        LexerToken current = nextToken(); // accept '[' token
        List<TypeParam> typeParams = new ArrayList<>();
        while (current.isIdent()) {
            TypeDecl typeDecl = parseTypeDecl();
            SourceSpan paramSpan = typeDecl;
            current = currentToken;
            TypeOper constraintOper = typeOperFor(current);
            Type constraintArg = null;
            if (constraintOper != null) {
                constraintArg = parseTypeExpr();
                current = currentToken;
                if (constraintArg == null) {
                    throw new ParserError(TYPE_EXPECTED, current);
                }
                paramSpan = typeDecl.adjoin(constraintArg);
            }
            typeParams.add(new TypeParam(typeDecl, constraintOper, constraintArg, paramSpan));
            if (!current.isOneCharSymbol(COMMA_CHAR)) {
                break;
            }
            current = nextToken(); // accept ',' token
        }
        if (!current.isOneCharSymbol(R_BRACKET_CHAR)) {
            throw new ParserError(R_BRACKET_EXPECTED, current);
        }
        nextToken(); // accept ']' token
        return typeParams;
    }

    private Type parseTypeTerm() {
        LexerToken current = currentToken;
        if (current.isIdent()) {
            LexerToken identToken = current;
            current = nextToken(); // accept IDENT token
            if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
                return parseTypeApply(identToken);
            } else {
                Ident name = tokenToIdent(identToken);
                Type type = Type.fromIdent(name, identToken);
                if (current.isOneCharSymbol(HASH_TAG_CHAR)) {
                    if (type instanceof LabelAsType labelType) {
                        return parseStructType(labelType);
                    } else {
                        throw new ParserError(LABEL_TYPE_EXPECTED, type);
                    }
                } else {
                    return type;
                }
            }
        } else if (current.isKeyword() || current.isStr() || current.isInt()) {
            if (current.isKeyword(PROC_VALUE)) {
                return parseProcType();
            } else if (current.isKeyword(FUNC_VALUE)) {
                return parseFuncType();
            } else {
                Type typeExpr;
                if (current.isKeyword(TRUE_VALUE)) {
                    typeExpr = new BoolAsType(Bool.TRUE, current);
                } else if (current.isKeyword(FALSE_VALUE)) {
                    typeExpr = new BoolAsType(Bool.FALSE, current);
                } else if (current.isKeyword(NULL_VALUE)) {
                    typeExpr = new NullAsType(current);
                } else if (current.isKeyword(EOF_VALUE)) {
                    typeExpr = new EofAsType(current);
                } else if (current.isStr()) {
                    String substring = Str.unquote(current.source(), current.sourceBegin(), current.sourceEnd());
                    typeExpr = new StrAsType(Str.of(substring), current);
                } else if (current.isInt()) {
                    String symbolText = current.substring();
                    typeExpr = new Int64AsType(symbolText, current);
                } else {
                    throw new ParserError(TYPE_EXPECTED, current);
                }
                nextToken(); // accept label token
                if (current.isOneCharSymbol(HASH_TAG_CHAR)) {
                    if (typeExpr instanceof LabelAsType labelType) {
                        return parseStructType(labelType);
                    } else {
                        throw new ParserError(LABEL_TYPE_EXPECTED, current);
                    }
                } else {
                    return typeExpr;
                }
            }
        } else if (current.isOneCharSymbol(L_BRACKET_CHAR)) {
            return parseTupleTypeExpr(null);
        } else if (current.isOneCharSymbol(L_BRACE_CHAR)) {
            return parseRecTypeExpr(null);
        }
        return null;
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
        // If the operator is '-' and the operand is an unsigned number, simplify the expression as a negative number.
        if (unaryOper == UnaryOper.NEGATE && right instanceof NumAsExpr numAsExpr) {
            if (numAsExpr instanceof Int64AsExpr intAsExpr && Character.isDigit(intAsExpr.intText().charAt(0))) {
                return new Int64AsExpr("-" + intAsExpr.intText(), operToken.adjoin(intAsExpr));
            } else if (numAsExpr instanceof Flt64AsExpr fltAsExpr && Character.isDigit(fltAsExpr.fltText().charAt(0))) {
                return new Flt64AsExpr("-" + fltAsExpr.fltText(), operToken.adjoin(fltAsExpr));
            } else if (numAsExpr instanceof Dec128AsExpr decAsExpr && Character.isDigit(decAsExpr.decText().charAt(0))) {
                return new Dec128AsExpr("-" + decAsExpr.decText(), operToken.adjoin(decAsExpr));
            } else {
                return new UnaryExpr(unaryOper, right, operToken.adjoin(right));
            }
        } else {
            return new UnaryExpr(unaryOper, right, operToken.adjoin(right));
        }
    }

    private Type parseUnionType() {
        Type answer = parseIntersectionType();
        if (answer == null) {
            return null;
        }
        while (currentToken.isOneCharSymbol(OR_OPER_CHAR)) {
            nextToken(); // accept '|' token
            Type right = parseIntersectionType();
            if (right == null) {
                throw new ParserError(TYPE_EXPECTED, currentToken);
            }
            answer = new UnionType(answer, right, answer.adjoin(right));
        }
        return answer;
    }

    private StmtOrExpr parseValueOrIdentAsExpr() {
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
            String substring = Str.unquote(current.source(), current.sourceBegin(), current.sourceEnd());
            StrAsExpr strAsExpr = new StrAsExpr(Str.of(substring), current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                return parseStructExpr(strAsExpr);
            }
            return strAsExpr;
        }
        if (current.isIdent()) {
            LexerToken next = nextToken(); // accept IDENT token
            Ident ident = tokenToIdent(current);
            IdentAsExpr identAsExpr = new IdentAsExpr(ident, current);
            if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                if (ident.name.equals(META_VALUE)) {
                    return parseMetaStructAndNextStmtOrExpr(identAsExpr);
                } else {
                    return parseStructExpr(identAsExpr);
                }
            } else {
                return identAsExpr;
            }
        }
        if (current.isInt()) {
            nextToken(); // accept Int token
            String symbolText = current.substring();
            return new Int64AsExpr(symbolText, current);
        }
        if (current.isFlt()) {
            nextToken();  // accept Flt token
            String symbolText = current.substring();
            return new Flt64AsExpr(symbolText, current);
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
                    return parseStructExpr(boolAsExpr);
                }
                return boolAsExpr;
            }
            if (current.substringEquals(FALSE_VALUE)) {
                LexerToken next = nextToken();  // accept 'false' token
                BoolAsExpr boolAsExpr = new BoolAsExpr(Bool.FALSE, current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseStructExpr(boolAsExpr);
                }
                return boolAsExpr;
            }
            if (current.substringEquals(NULL_VALUE)) {
                LexerToken next = nextToken(); // accept 'null' token
                NullAsExpr nullAsExpr = new NullAsExpr(current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseStructExpr(nullAsExpr);
                }
                return nullAsExpr;
            }
            if (current.substringEquals(EOF_VALUE)) {
                LexerToken next = nextToken(); // accept 'EOF' token
                EofAsExpr eofAsExpr = new EofAsExpr(current);
                if (next.isOneCharSymbol(HASH_TAG_CHAR)) {
                    return parseStructExpr(eofAsExpr);
                }
                return eofAsExpr;
            }
        }
        if (current.isChar()) {
            return parseChar();
        }
        return null;
    }

    private VarDecl parseVarDecl() {
        LexerToken current = currentToken;
        Pat pat = parsePat();
        if (pat == null) {
            throw new ParserError(PAT_EXPECTED, current);
        }
        current = currentToken;
        if (current.isOneCharSymbol(UNIFY_OPER_CHAR)) {
            nextToken(); // accept '=' token
            StmtOrExpr expr = parseStmtOrExpr();
            if (expr == null) {
                throw new ParserError(EXPR_EXPECTED, current);
            }
            return new InitVarDecl(pat, expr, pat.adjoin(expr));
        } else if (pat instanceof IdentAsPat identAsPat) {
            return new IdentVarDecl(identAsPat, identAsPat);
        } else {
            throw new ParserError(IDENT_EXPECTED, pat);
        }
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

    public final SourceString source() {
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
        SourceString source = source();
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

    private TypeOper typeOperFor(LexerToken operToken) {
        if (operToken.isTwoCharSymbol()) {
            if (operToken.isTwoCharSymbol(SUBTYPE_OPER)) {
                return TypeOper.SUBTYPE;
            }
            if (operToken.isTwoCharSymbol(TYPE_OPER)) {
                return TypeOper.TYPE;
            }
            if (operToken.isTwoCharSymbol(SUPERTYPE_OPER)) {
                return TypeOper.SUPERTYPE;
            }
        }
        return null;
    }

    private enum SelectOrApply {
        DOT,
        INDEX,
        APPLY
    }

}
