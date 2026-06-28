package logicscript.ls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LsLexer {

    private static final Map<String, LsTokenType> KEYWORDS = Map.ofEntries(
            Map.entry("module", LsTokenType.MODULE),
            Map.entry("use", LsTokenType.USE),
            Map.entry("atom", LsTokenType.ATOM),
            Map.entry("formula", LsTokenType.FORMULA),
            Map.entry("nl", LsTokenType.NL),
            Map.entry("let", LsTokenType.LET),
            Map.entry("translate", LsTokenType.TRANSLATE),
            Map.entry("vertauto", LsTokenType.VERTAUTO),
            Map.entry("not", LsTokenType.NOT),
            Map.entry("and", LsTokenType.AND),
            Map.entry("or", LsTokenType.OR),
            Map.entry("impl", LsTokenType.IMPL),
            Map.entry("iff", LsTokenType.IFF),
            Map.entry("steps", LsTokenType.STEPS),
            Map.entry("table", LsTokenType.TABLE),
            Map.entry("verdict", LsTokenType.VERDICT)
    );

    private final String input;
    private int pos;
    private int line;

    public LsLexer(String input) {
        this.input = input == null ? "" : input;
        this.pos = 0;
        this.line = 1;
    }

    public List<LsToken> tokenize() {
        List<LsToken> tokens = new ArrayList<>();
        while (true) {
            LsToken token = nextToken();
            tokens.add(token);
            if (token.type() == LsTokenType.EOF) {
                break;
            }
        }
        return tokens;
    }

    private LsToken nextToken() {
        skipWhitespaceAndComments();
        int startLine = line;
        if (pos >= input.length()) {
            return new LsToken(LsTokenType.EOF, "", startLine);
        }
        char c = input.charAt(pos);
        return switch (c) {
            case '=' -> single(LsTokenType.EQ, startLine);
            case ':' -> single(LsTokenType.COLON, startLine);
            case ',' -> single(LsTokenType.COMMA, startLine);
            case '(' -> single(LsTokenType.LPAREN, startLine);
            case ')' -> single(LsTokenType.RPAREN, startLine);
            case '{' -> single(LsTokenType.LBRACE, startLine);
            case '}' -> single(LsTokenType.RBRACE, startLine);
            case '"' -> readString(startLine);
            default -> {
                if (Character.isLetter(c) || c == '_') {
                    yield readIdentOrKeyword(startLine);
                }
                throw new LsParseException("Carácter no reconocido '" + c + "' en línea " + startLine);
            }
        };
    }

    private LsToken single(LsTokenType type, int startLine) {
        String lex = String.valueOf(input.charAt(pos++));
        return new LsToken(type, lex, startLine);
    }

    private void skipWhitespaceAndComments() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '#') {
                while (pos < input.length() && input.charAt(pos) != '\n') {
                    pos++;
                }
                continue;
            }
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
                continue;
            }
            if (c == '\n') {
                line++;
                pos++;
                continue;
            }
            break;
        }
    }

    private LsToken readString(int startLine) {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') {
                return new LsToken(LsTokenType.STRING, sb.toString(), startLine);
            }
            if (c == '\\' && pos < input.length()) {
                char esc = input.charAt(pos++);
                sb.append(switch (esc) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> esc;
                });
            } else {
                sb.append(c);
            }
        }
        throw new LsParseException("Cadena sin cerrar en línea " + startLine);
    }

    private LsToken readIdentOrKeyword(int startLine) {
        int start = pos;
        pos++;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                pos++;
            } else {
                break;
            }
        }
        String text = input.substring(start, pos);
        LsTokenType type = KEYWORDS.getOrDefault(text, LsTokenType.IDENT);
        return new LsToken(type, text, startLine);
    }
}
