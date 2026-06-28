package logicscript.ls;

import logicscript.ls.ast.LsProgram;
import java.util.List;

public final class LsCheckHarness {

    private LsCheckHarness() {
    }

    public static void main(String[] args) {
        int fallos = 0;
        fallos += probarOk("examples/plantilla_ejercicio.ls");
        fallos += probarOk("examples/ejercicio_lluvia.ls");
        fallos += probarOk("examples/tautologia_estudio.ls");
        fallos += probarErrorEnFuente(
                """
                module m
                let x = translate(falta)
                """,
                "Texto nl no definido"
        );
        fallos += probarErrorEnFuente(
                """
                module m
                formula f = p or q
                vertauto f
                """,
                "Átomo no declarado"
        );
        fallos += probarErrorEnFuente(
                """
                module m
                atom p = "a"
                atom p = "b"
                """,
                "Redeclaración"
        );
        fallos += probarErrorEnFuente(
                """
                module m
                vertauto desconocida
                """,
                "Fórmula no definida"
        );
        if (fallos > 0) {
            System.err.println("LsCheckHarness: " + fallos + " fallo(s)");
            System.exit(1);
        }
        System.out.println("LsCheckHarness: OK (7 casos)");
    }

    private static int probarOk(String archivo) {
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            java.io.PrintStream originalOut = System.out;
            try {
                System.setOut(new java.io.PrintStream(out, true, java.nio.charset.StandardCharsets.UTF_8));
                if (!LsChecker.checkArchivo(archivo)) {
                    System.setOut(originalOut);
                    System.err.println(archivo + ": se esperaba OK");
                    return 1;
                }
            } finally {
                System.setOut(originalOut);
            }
            return 0;
        } catch (RuntimeException e) {
            System.err.println(archivo + ": " + e.getMessage());
            return 1;
        }
    }

    private static int probarErrorEnFuente(String fuente, String fragmento) {
        try {
            LsProgram program = LsParser.parse(fuente);
            List<String> errores = LsValidator.validar(program);
            if (errores.isEmpty()) {
                System.err.println("Se esperaba error con: " + fragmento);
                return 1;
            }
            String todas = String.join("\n", errores);
            if (!todas.contains(fragmento)) {
                System.err.println("Se esperaba '" + fragmento + "' en: " + todas);
                return 1;
            }
            return 0;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains(fragmento)) {
                return 0;
            }
            System.err.println("Error inesperado: " + e.getMessage());
            return 1;
        }
    }
}
