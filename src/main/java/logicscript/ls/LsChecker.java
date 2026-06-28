package logicscript.ls;

import java.util.List;

public final class LsChecker {

    private LsChecker() {
    }

    public static boolean checkArchivo(String ruta) {
        List<String> errores = LsValidator.validarArchivo(ruta);
        if (!errores.isEmpty()) {
            for (String error : errores) {
                System.err.println(error);
            }
            return false;
        }
        System.out.println("OK: " + ruta);
        return true;
    }
}
