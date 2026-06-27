package logicscript.vertauto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Resultado de {@link VertautoService}: fórmula evaluada, dictamen y datos opcionales de tabla/pasos.
 */
public final class VertautoResult {

    private final String formula;
    private final TipoDictamen dictamen;
    private final List<String> atomos;
    private final List<String> filasTabla;
    private final List<String> pasos;

    public VertautoResult(
            String formula,
            TipoDictamen dictamen,
            List<String> atomos,
            List<String> filasTabla,
            List<String> pasos
    ) {
        this.formula = Objects.requireNonNull(formula);
        this.dictamen = Objects.requireNonNull(dictamen);
        this.atomos = List.copyOf(atomos);
        this.filasTabla = List.copyOf(filasTabla);
        this.pasos = List.copyOf(pasos);
    }

    public String getFormula() {
        return formula;
    }

    public TipoDictamen getDictamen() {
        return dictamen;
    }

    public List<String> getAtomos() {
        return atomos;
    }

    public List<String> getFilasTabla() {
        return filasTabla;
    }

    public List<String> getPasos() {
        return pasos;
    }

    /** Salida modo corto: {@code (p → q)  contingency} */
    public String formatoCorto() {
        return formula + "  " + dictamen.etiquetaCorta();
    }

    public void imprimirModoCorto() {
        System.out.println(formatoCorto());
    }

    public void imprimirExtendido(boolean steps, boolean table, boolean verdict) {
        if (steps && !pasos.isEmpty()) {
            System.out.println("=== Pasos ===");
            for (String paso : pasos) {
                System.out.println("  - " + paso);
            }
            System.out.println();
        }
        if (table && !filasTabla.isEmpty()) {
            System.out.println("=== Tabla de verdad ===");
            for (String fila : filasTabla) {
                System.out.println(fila);
            }
            System.out.println();
        }
        if (verdict) {
            System.out.println("=== Dictamen ===");
            System.out.println(formatoCorto());
            System.out.println("Clasificación: " + dictamen.etiquetaCorta());
        }
    }
}
