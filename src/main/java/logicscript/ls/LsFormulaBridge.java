package logicscript.ls;

import logicscript.ir.AndExpr;
import logicscript.ir.AtomExpr;
import logicscript.ir.EquivExpr;
import logicscript.ir.ImpExpr;
import logicscript.ir.LogicExpr;
import logicscript.ir.NegExpr;
import logicscript.ir.OrExpr;
import logicscript.ls.ast.AtomRef;
import logicscript.ls.ast.BinaryExpr;
import logicscript.ls.ast.BinaryOp;
import logicscript.ls.ast.FormulaExpr;
import logicscript.ls.ast.NotExpr;

public final class LsFormulaBridge {

    private LsFormulaBridge() {
    }

    public static LogicExpr toLogicExpr(FormulaExpr expr) {
        if (expr instanceof AtomRef a) {
            return new AtomExpr(a.name(), false);
        }
        if (expr instanceof NotExpr n) {
            return new NegExpr(toLogicExpr(n.inner()));
        }
        if (expr instanceof BinaryExpr b) {
            LogicExpr left = toLogicExpr(b.left());
            LogicExpr right = toLogicExpr(b.right());
            return switch (b.op()) {
                case AND -> new AndExpr(left, right);
                case OR -> new OrExpr(left, right);
                case IMPL -> new ImpExpr(left, right);
                case IFF -> new EquivExpr(left, right);
            };
        }
        throw new IllegalStateException("Expresión no contemplada: " + expr.getClass());
    }

    public static String toDisplayString(FormulaExpr expr) {
        if (expr instanceof AtomRef a) {
            return a.name();
        }
        if (expr instanceof NotExpr n) {
            return "not " + toDisplayString(n.inner());
        }
        if (expr instanceof BinaryExpr b) {
            String op = switch (b.op()) {
                case AND -> "and";
                case OR -> "or";
                case IMPL -> "impl";
                case IFF -> "iff";
            };
            return toDisplayString(b.left()) + " " + op + " " + toDisplayString(b.right());
        }
        throw new IllegalStateException("Expresión no contemplada: " + expr.getClass());
    }
}
