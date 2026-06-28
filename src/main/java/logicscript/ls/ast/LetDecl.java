package logicscript.ls.ast;

public record LetDecl(String name, TranslateCall translate) implements LsStmt {
}
