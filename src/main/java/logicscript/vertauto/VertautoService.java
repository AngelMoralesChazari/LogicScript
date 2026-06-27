package logicscript.vertauto;

import logicscript.LogicScriptResult;
import logicscript.LogicScriptService;
import logicscript.ir.LogicExpr;

/**
 * API pública de {@code vertauto}: evalúa fórmulas en notación clásica o sintaxis {@code .ls}.
 */
public final class VertautoService {

    private final LogicScriptService translateService;

    public VertautoService() {
        this(new LogicScriptService());
    }

    public VertautoService(LogicScriptService translateService) {
        this.translateService = translateService;
    }

    /** Evalúa cadena en notación clásica ({@code (p → q)}, {@code (p ∨ ¬p)}, …). */
    public VertautoResult vertautoClasica(String formulaClasica) {
        LogicExpr expr = ParserNotacionClasica.parsear(formulaClasica);
        return EvaluadorProposicional.evaluar(expr, formulaClasica.trim());
    }

    /** Evalúa expresión en sintaxis LogicScript ({@code p or not p}, {@code p impl q}, …). */
    public VertautoResult vertautoExpr(String expresionLogicScript) {
        LogicExpr expr = ParserFormulaLogicScript.parsear(expresionLogicScript);
        String mostrada = expresionLogicScript.trim();
        return EvaluadorProposicional.evaluar(expr, mostrada);
    }

    /** Traduce NL y evalúa la fórmula emitida (incluye pasos de traducción en el resultado). */
    public VertautoResult vertautoDesdeNl(String textoNatural) {
        LogicScriptResult traduccion = translateService.traducir(textoNatural);
        if (!traduccion.isExito()) {
            throw new IllegalArgumentException(traduccion.getMensaje());
        }
        VertautoResult evaluacion = vertautoClasica(traduccion.getFormula());
        return new VertautoResult(
                evaluacion.getFormula(),
                evaluacion.getDictamen(),
                evaluacion.getAtomos(),
                evaluacion.getFilasTabla(),
                evaluacion.getPasosEvaluacion(),
                textoNatural.trim(),
                traduccion.getPasosDeAnalisis(),
                traduccion.getProposiciones()
        );
    }

    public VertautoResult vertauto(LogicExpr expr, String formulaMostrada) {
        return EvaluadorProposicional.evaluar(expr, formulaMostrada);
    }
}
