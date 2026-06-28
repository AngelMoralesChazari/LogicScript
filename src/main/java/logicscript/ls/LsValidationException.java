package logicscript.ls;

import java.util.List;

public class LsValidationException extends RuntimeException {

    private final List<String> errores;

    public LsValidationException(List<String> errores) {
        super(formatear(errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }

    private static String formatear(List<String> errores) {
        if (errores.size() == 1) {
            return errores.get(0);
        }
        StringBuilder sb = new StringBuilder(errores.size() + " error(es) de validación:");
        for (String error : errores) {
            sb.append("\n  - ").append(error);
        }
        return sb.toString();
    }
}
