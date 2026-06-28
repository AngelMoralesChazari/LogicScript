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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LsInterpreter {

    private static final Set<String> MODULOS_CONOCIDOS = Set.of("core");

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
        validarModulos(program);
        Map<String, String> nlTextos = new HashMap<>();
        Map<String, String> atomEtiquetas = new HashMap<>();
        Map<String, FormulaBinding> formulas = new HashMap<>();

        for (LsStmt stmt : program.statements()) {
            if (stmt instanceof AtomDecl) {
                AtomDecl a = (AtomDecl) stmt;
                atomEtiquetas.put(a.name(), a.label());
            } else if (stmt instanceof NlDecl) {
                NlDecl n = (NlDecl) stmt;
                nlTextos.put(n.name(), n.text());
            } else if (stmt instanceof LetDecl) {
                LetDecl l = (LetDecl) stmt;
                formulas.put(l.name(), evaluarTranslate(l.translate(), nlTextos));
            } else if (stmt instanceof FormulaDecl) {
                FormulaDecl f = (FormulaDecl) stmt;
                formulas.put(f.name(), evaluarFormula(f.expression()));
            } else if (stmt instanceof VertautoStmt) {
                VertautoStmt v = (VertautoStmt) stmt;
                ejecutarVertauto(v, formulas);
            } else {
                throw new IllegalStateException("Sentencia no contemplada: " + stmt.getClass());
            }
        }
    }

    public void ejecutarArchivo(String ruta) {
        String source = LsSourceLoader.leer(ruta);
        LsProgram program = LsParser.parse(source);
        ejecutar(program);
    }

    private void validarModulos(LsProgram program) {
        Set<String> desconocidos = new HashSet<>();
        for (String use : program.uses()) {
            if (!MODULOS_CONOCIDOS.contains(use)) {
                desconocidos.add(use);
            }
        }
        if (!desconocidos.isEmpty()) {
            throw new LsRuntimeException("Módulo(s) desconocido(s): " + desconocidos
                    + ". En v1 solo está soportado: core");
        }
    }

    private FormulaBinding evaluarTranslate(TranslateCall call, Map<String, String> nlTextos) {
        if (call.moduleOverride() != null) {
            throw new LsRuntimeException("translate con módulo explícito aún no implementado: "
                    + call.moduleOverride());
        }
        String texto = resolverTexto(call.text(), nlTextos);
        LogicScriptResult traduccion = translateService.traducir(texto);
        if (!traduccion.isExito()) {
            throw new LsRuntimeException("translate falló: " + traduccion.getMensaje());
        }
        return FormulaBinding.desdeTraduccion(traduccion, texto);
    }

    private FormulaBinding evaluarFormula(FormulaExpr expr) {
        LogicExpr logicExpr = LsFormulaBridge.toLogicExpr(expr);
        String mostrada = LsFormulaBridge.toDisplayString(expr);
        return FormulaBinding.desdeExpresion(logicExpr, mostrada);
    }

    private void ejecutarVertauto(VertautoStmt stmt, Map<String, FormulaBinding> formulas) {
        FormulaBinding binding = resolverFormula(stmt.target(), formulas);
        VertautoResult resultado = binding.evaluar(vertautoService);
        VertautoOptions opts = stmt.options();
        if (!opts.steps() && !opts.table() && !opts.verdict()) {
            resultado.imprimirModoCorto();
        } else {
            resultado.imprimirExtendido(opts.steps(), opts.table(), opts.verdict());
        }
    }

    private FormulaBinding resolverFormula(FormulaRef ref, Map<String, FormulaBinding> formulas) {
        if (ref.isVariable()) {
            FormulaBinding binding = formulas.get(ref.variableName());
            if (binding == null) {
                throw new LsRuntimeException("Fórmula no definida: " + ref.variableName());
            }
            return binding;
        }
        return evaluarFormula(ref.inlineExpression());
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

        private FormulaBinding(
                String formulaClasica,
                LogicExpr expr,
                String formulaMostrada,
                LogicScriptResult traduccion,
                String textoNatural
        ) {
            this.formulaClasica = formulaClasica;
            this.expr = expr;
            this.formulaMostrada = formulaMostrada;
            this.traduccion = traduccion;
            this.textoNatural = textoNatural;
        }

        static FormulaBinding desdeTraduccion(LogicScriptResult traduccion, String textoNatural) {
            return new FormulaBinding(
                    traduccion.getFormula(),
                    null,
                    traduccion.getFormula(),
                    traduccion,
                    textoNatural
            );
        }

        static FormulaBinding desdeExpresion(LogicExpr expr, String mostrada) {
            return new FormulaBinding(null, expr, mostrada, null, null);
        }

        VertautoResult evaluar(VertautoService vertautoService) {
            if (traduccion != null) {
                VertautoResult evaluacion = vertautoService.vertautoClasica(traduccion.getFormula());
                return new VertautoResult(
                        evaluacion.getFormula(),
                        evaluacion.getDictamen(),
                        evaluacion.getAtomos(),
                        evaluacion.getFilasTabla(),
                        evaluacion.getPasosEvaluacion(),
                        textoNatural,
                        traduccion.getPasosDeAnalisis(),
                        traduccion.getProposiciones()
                );
            }
            if (expr != null) {
                return vertautoService.vertauto(expr, formulaMostrada);
            }
            return vertautoService.vertautoClasica(formulaClasica);
        }
    }
}
