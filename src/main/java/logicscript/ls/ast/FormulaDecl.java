package logicscript.ls.ast;

public record FormulaDecl(String name, FormulaExpr expression) implements LsStmt {
}
