package logicscript.ls;

import logicscript.LogicScriptResult;
import logicscript.LogicScriptService;
import logicscript.ir.LogicExpr;
import logicscript.ls.ast.AtomDecl;
import logicscript.ls.ast.FormulaDecl;
import logicscript.ls.ast.FormulaExpr;
import logicscript.ls.ast.FormulaRef;
import logicscript.ls.ast.LetDecl;
import logicscript.ls.ast.LsProgram;
import logicscript.ls.ast.LsStmt;
import logicscript.ls.ast.NlDecl;
import logicscript.ls.ast.TextRef;
import logicscript.ls.ast.TranslateCall;
import logicscript.ls.ast.VertautoOptions;
import logicscript.ls.ast.VertautoStmt;
import logicscript.vertauto.VertautoResult;
import logicscript.vertauto.VertautoService;
import logicscript.vertauto.EvaluadorProposicional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LsInterpreter {

    private final LogicScriptService translateService;
    private final VertautoService vertautoService;

    public LsInterpreter() {
        this(new LogicScriptService(), new VertautoService());
    }

    public LsInterpreter(LogicScriptService translateService, VertautoService vertautoService) {
        this.translateService = translateService;
        this.vertautoService = vertautoService;
    }

    public void ejecutar(LsProgram program) {
        LsValidator.validarOError(program);
        Map<String, String> nlTextos = new HashMap<>();
        Map<String, String> atomEtiquetas = new LinkedHashMap<>();
        Map<String, FormulaBinding> formulas = new HashMap<>();

        for (LsStmt stmt : program.statements()) {
            LsAtomRegistry atomRegistry = new LsAtomRegistry(atomEtiquetas);
            if (stmt instanceof AtomDecl) {
                AtomDecl a = (AtomDecl) stmt;
                atomEtiquetas.put(a.name(), a.label());
            } else if (stmt instanceof NlDecl) {
                NlDecl n = (NlDecl) stmt;
                nlTextos.put(n.name(), n.text());
            } else if (stmt instanceof LetDecl) {
                LetDecl l = (LetDecl) stmt;
                formulas.put(l.name(), evaluarTranslate(l.translate(), nlTextos, atomRegistry));
            } else if (stmt instanceof FormulaDecl) {
                FormulaDecl f = (FormulaDecl) stmt;
                formulas.put(f.name(), evaluarFormula(f.expression(), atomRegistry));
            } else if (stmt instanceof VertautoStmt) {
                VertautoStmt v = (VertautoStmt) stmt;
                ejecutarVertauto(v, formulas, atomRegistry);
            } else {
                throw new IllegalStateException("Sentencia no contemplada: " + stmt.getClass());
            }
        }
    }

    public void ejecutarArchivo(String ruta) {
        String source = LsSourceLoader.leer(ruta);
        LsProgram program = LsParser.parse(source);
        List<String> errores = new ArrayList<>();
        LsValidator.validarArchivoConvencion(ruta, program.moduleName(), errores);
        if (!errores.isEmpty()) {
            throw new LsValidationException(errores);
        }
        ejecutar(program);
    }

    private FormulaBinding evaluarTranslate(
            TranslateCall call,
            Map<String, String> nlTextos,
            LsAtomRegistry atomRegistry
    ) {
        if (call.moduleOverride() != null) {
            throw new LsRuntimeException("translate con módulo explícito aún no implementado: "
                    + call.moduleOverride());
        }
        String texto = resolverTexto(call.text(), nlTextos);
        LogicScriptResult traduccion = translateService.traducir(texto);
        if (!traduccion.isExito()) {
            throw new LsRuntimeException("translate falló: " + traduccion.getMensaje());
        }
        return FormulaBinding.desdeTraduccion(traduccion, texto, atomRegistry);
    }

    private FormulaBinding evaluarFormula(FormulaExpr expr, LsAtomRegistry atomRegistry) {
        LogicExpr logicExpr = LsFormulaBridge.toLogicExpr(expr);
        String mostrada = LsFormulaBridge.emitirFormulaClasica(expr, atomRegistry);
        return FormulaBinding.desdeExpresion(logicExpr, mostrada, atomRegistry);
    }

    private void ejecutarVertauto(
            VertautoStmt stmt,
            Map<String, FormulaBinding> formulas,
            LsAtomRegistry atomRegistry
    ) {
        FormulaBinding binding = resolverFormula(stmt.target(), formulas, atomRegistry);
        VertautoResult resultado = binding.evaluar(vertautoService);
        VertautoOptions opts = stmt.options();
        if (!opts.steps() && !opts.table() && !opts.verdict()) {
            resultado.imprimirModoCorto();
        } else {
            resultado.imprimirExtendido(opts.steps(), opts.table(), opts.verdict());
        }
    }

    private FormulaBinding resolverFormula(
            FormulaRef ref,
            Map<String, FormulaBinding> formulas,
            LsAtomRegistry atomRegistry
    ) {
        if (ref.isVariable()) {
            FormulaBinding binding = formulas.get(ref.variableName());
            if (binding == null) {
                throw new LsRuntimeException("Fórmula no definida: " + ref.variableName());
            }
            return binding;
        }
        return evaluarFormula(ref.inlineExpression(), atomRegistry);
    }

    private String resolverTexto(TextRef ref, Map<String, String> nlTextos) {
        if (ref.isLiteral()) {
            return ref.literal();
        }
        String texto = nlTextos.get(ref.variableName());
        if (texto == null) {
            throw new LsRuntimeException("Texto nl no definido: " + ref.variableName());
        }
        return texto;
    }

    static final class FormulaBinding {
        private final String formulaClasica;
        private final LogicExpr expr;
        private final String formulaMostrada;
        private final LogicScriptResult traduccion;
        private final String textoNatural;
        private final LsAtomRegistry atomRegistry;

        private FormulaBinding(
                String formulaClasica,
                LogicExpr expr,
                String formulaMostrada,
                LogicScriptResult traduccion,
                String textoNatural,
                LsAtomRegistry atomRegistry
        ) {
            this.formulaClasica = formulaClasica;
            this.expr = expr;
            this.formulaMostrada = formulaMostrada;
            this.traduccion = traduccion;
            this.textoNatural = textoNatural;
            this.atomRegistry = atomRegistry == null ? LsAtomRegistry.vacio() : atomRegistry;
        }

        static FormulaBinding desdeTraduccion(
                LogicScriptResult traduccion,
                String textoNatural,
                LsAtomRegistry atomRegistry
        ) {
            return new FormulaBinding(
                    traduccion.getFormula(),
                    null,
                    traduccion.getFormula(),
                    traduccion,
                    textoNatural,
                    atomRegistry
            );
        }

        static FormulaBinding desdeExpresion(
                LogicExpr expr,
                String mostrada,
                LsAtomRegistry atomRegistry
        ) {
            return new FormulaBinding(null, expr, mostrada, null, null, atomRegistry);
        }

        VertautoResult evaluar(VertautoService vertautoService) {
            if (traduccion != null) {
                VertautoResult evaluacion = vertautoService.vertautoClasica(traduccion.getFormula());
                Map<String, String> proposiciones = atomRegistry.proposicionesDesdeTraduccion(
                        traduccion.getProposiciones()
                );
                return new VertautoResult(
                        evaluacion.getFormula(),
                        evaluacion.getDictamen(),
                        evaluacion.getAtomos(),
                        evaluacion.getFilasTabla(),
                        evaluacion.getPasosEvaluacion(),
                        textoNatural,
                        traduccion.getPasosDeAnalisis(),
                        proposiciones
                );
            }
            if (expr != null) {
                VertautoResult evaluacion = vertautoService.vertauto(expr, formulaMostrada);
                List<String> simbolos = EvaluadorProposicional.recolectarAtomos(expr);
                Map<String, String> proposiciones = atomRegistry.proposicionesParaSimbolos(simbolos);
                if (proposiciones.isEmpty()) {
                    return evaluacion;
                }
                return new VertautoResult(
                        evaluacion.getFormula(),
                        evaluacion.getDictamen(),
                        evaluacion.getAtomos(),
                        evaluacion.getFilasTabla(),
                        evaluacion.getPasosEvaluacion(),
                        null,
                        List.of(),
                        proposiciones
                );
            }
            return vertautoService.vertautoClasica(formulaClasica);
        }
    }
}
