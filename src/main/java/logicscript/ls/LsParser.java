package logicscript.ls;

import logicscript.ls.ast.AtomDecl;
import logicscript.ls.ast.AtomRef;
import logicscript.ls.ast.BinaryExpr;
import logicscript.ls.ast.BinaryOp;
import logicscript.ls.ast.FormulaDecl;
import logicscript.ls.ast.FormulaExpr;
import logicscript.ls.ast.FormulaRef;
import logicscript.ls.ast.LetDecl;
import logicscript.ls.ast.LsProgram;
import logicscript.ls.ast.LsStmt;
import logicscript.ls.ast.NlDecl;
import logicscript.ls.ast.NotExpr;
import logicscript.ls.ast.TextRef;
import logicscript.ls.ast.TranslateCall;
import logicscript.ls.ast.VertautoOptions;
import logicscript.ls.ast.VertautoStmt;
import java.util.ArrayList;
import java.util.List;

public final class LsParser {

    private final List<LsToken> tokens;
    private int pos;

    public LsParser(List<LsToken> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public static LsProgram parse(String source) {
        LsLexer lexer = new LsLexer(source);
        return new LsParser(lexer.tokenize()).parseProgram();
    }

    private LsProgram parseProgram() {
        expect(LsTokenType.MODULE);
        String moduleName = expectIdent("nombre de módulo");
        List<String> uses = new ArrayList<>();
        List<LsStmt> statements = new ArrayList<>();

        while (!check(LsTokenType.EOF)) {
            if (match(LsTokenType.USE)) {
                uses.add(expectIdent("módulo en use"));
                continue;
            }
            statements.add(parseStatement());
        }
        return new LsProgram(moduleName, List.copyOf(uses), List.copyOf(statements));
    }

    private LsStmt parseStatement() {
        if (match(LsTokenType.ATOM)) {
            String name = expectIdent("átomo");
            expect(LsTokenType.EQ);
            String label = expectString("etiqueta de átomo");
            return new AtomDecl(name, label);
        }
        if (match(LsTokenType.NL)) {
            String name = expectIdent("variable nl");
            expect(LsTokenType.EQ);
            String text = expectString("texto nl");
            return new NlDecl(name, text);
        }
        if (match(LsTokenType.LET)) {
            String name = expectIdent("variable let");
            expect(LsTokenType.EQ);
            TranslateCall call = parseTranslateCall();
            return new LetDecl(name, call);
        }
        if (match(LsTokenType.FORMULA)) {
            String name = expectIdent("fórmula");
            expect(LsTokenType.EQ);
            FormulaExpr expr = parseFormulaExpr();
            return new FormulaDecl(name, expr);
        }
        if (match(LsTokenType.VERTAUTO)) {
            FormulaRef target = parseFormulaRef();
            VertautoOptions options = parseVertautoOptions();
            return new VertautoStmt(target, options);
        }
        throw error("se esperaba atom, nl, let, formula o vertauto");
    }

    private TranslateCall parseTranslateCall() {
        expect(LsTokenType.TRANSLATE);
        expect(LsTokenType.LPAREN);
        TextRef text = parseTextRef();
        String moduleOverride = null;
        if (match(LsTokenType.COMMA)) {
            moduleOverride = expectIdent("módulo en translate");
        }
        expect(LsTokenType.RPAREN);
        return new TranslateCall(text, moduleOverride);
    }

    private TextRef parseTextRef() {
        if (match(LsTokenType.STRING)) {
            return TextRef.literal(previous().lexeme());
        }
        if (check(LsTokenType.IDENT)) {
            return TextRef.variable(advance().lexeme());
        }
        throw error("se esperaba cadena o identificador en translate");
    }

    private FormulaRef parseFormulaRef() {
        if (match(LsTokenType.LPAREN)) {
            FormulaExpr expr = parseFormulaExpr();
            expect(LsTokenType.RPAREN);
            return FormulaRef.inline(expr);
        }
        return FormulaRef.variable(expectIdent("referencia de fórmula"));
    }

    private VertautoOptions parseVertautoOptions() {
        if (!match(LsTokenType.LBRACE)) {
            return VertautoOptions.corto();
        }
        boolean steps = false;
        boolean table = false;
        boolean verdict = false;
        while (!check(LsTokenType.RBRACE) && !check(LsTokenType.EOF)) {
            if (match(LsTokenType.STEPS)) {
                steps = true;
            } else if (match(LsTokenType.TABLE)) {
                table = true;
            } else if (match(LsTokenType.VERDICT)) {
                verdict = true;
            } else {
                throw error("opción vertauto desconocida (steps, table, verdict)");
            }
        }
        expect(LsTokenType.RBRACE);
        return new VertautoOptions(steps, table, verdict);
    }

    private FormulaExpr parseFormulaExpr() {
        return parseIff();
    }

    private FormulaExpr parseIff() {
        FormulaExpr left = parseImpl();
        while (match(LsTokenType.IFF)) {
            left = new BinaryExpr(BinaryOp.IFF, left, parseImpl());
        }
        return left;
    }

    private FormulaExpr parseImpl() {
        FormulaExpr left = parseOr();
        while (match(LsTokenType.IMPL)) {
            left = new BinaryExpr(BinaryOp.IMPL, left, parseImpl());
        }
        return left;
    }

    private FormulaExpr parseOr() {
        FormulaExpr left = parseAnd();
        while (match(LsTokenType.OR)) {
            left = new BinaryExpr(BinaryOp.OR, left, parseAnd());
        }
        return left;
    }

    private FormulaExpr parseAnd() {
        FormulaExpr left = parseNot();
        while (match(LsTokenType.AND)) {
            left = new BinaryExpr(BinaryOp.AND, left, parseNot());
        }
        return left;
    }

    private FormulaExpr parseNot() {
        if (match(LsTokenType.NOT)) {
            return new NotExpr(parseNot());
        }
        return parsePrimary();
    }

    private FormulaExpr parsePrimary() {
        if (match(LsTokenType.LPAREN)) {
            FormulaExpr inner = parseFormulaExpr();
            expect(LsTokenType.RPAREN);
            return inner;
        }
        if (check(LsTokenType.IDENT)) {
            return new AtomRef(advance().lexeme());
        }
        throw error("se esperaba identificador o '(' en expresión de fórmula");
    }

    private boolean match(LsTokenType type) {
        if (check(type)) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean check(LsTokenType type) {
        if (isAtEnd()) {
            return type == LsTokenType.EOF;
        }
        return peek().type() == type;
    }

    private LsToken advance() {
        if (!isAtEnd()) {
            pos++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == LsTokenType.EOF;
    }

    private LsToken peek() {
        return tokens.get(pos);
    }

    private LsToken previous() {
        return tokens.get(pos - 1);
    }

    private void expect(LsTokenType type) {
        if (!match(type)) {
            throw error("se esperaba " + type);
        }
    }

    private String expectIdent(String contexto) {
        if (check(LsTokenType.IDENT)) {
            return advance().lexeme();
        }
        throw error("se esperaba identificador (" + contexto + ")");
    }

    private String expectString(String contexto) {
        if (match(LsTokenType.STRING)) {
            return previous().lexeme();
        }
        throw error("se esperaba cadena (" + contexto + ")");
    }

    private LsParseException error(String message) {
        LsToken token = peek();
        return new LsParseException("Error de sintaxis en línea " + token.line() + ": " + message);
    }
}
