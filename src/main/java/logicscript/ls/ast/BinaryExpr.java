package logicscript.ls.ast;

public record BinaryExpr(BinaryOp op, FormulaExpr left, FormulaExpr right) implements FormulaExpr {
}
