package logicscript.ls;

public final class LscCli {

    private static final String VERSION = "0.2.0";

    private LscCli() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            imprimirAyuda();
            System.exit(1);
        }
        String comando = args[0];
        if ("run".equals(comando)) {
            if (args.length < 2) {
                System.err.println("Uso: lsc run <archivo.ls>");
                System.exit(1);
            }
            try {
                new LsInterpreter().ejecutarArchivo(args[1]);
            } catch (LsParseException | LsRuntimeException | LsValidationException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } else if ("check".equals(comando)) {
            if (args.length < 2) {
                System.err.println("Uso: lsc check <archivo.ls>");
                System.exit(1);
            }
            try {
                if (!LsChecker.checkArchivo(args[1])) {
                    System.exit(1);
                }
            } catch (LsParseException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } else if ("--version".equals(comando) || "-V".equals(comando)) {
            System.out.println("lsc " + VERSION);
        } else if ("help".equals(comando) || "--help".equals(comando) || "-h".equals(comando)) {
            imprimirAyuda();
        } else {
            System.err.println("Comando desconocido: " + comando);
            imprimirAyuda();
            System.exit(1);
        }
    }

    private static void imprimirAyuda() {
        System.out.println("LogicScript compiler / intérprete (lsc) " + VERSION);
        System.out.println();
        System.out.println("Uso:");
        System.out.println("  lsc run <archivo.ls>    Ejecuta un programa .ls");
        System.out.println("  lsc check <archivo.ls>  Valida sintaxis y referencias (sin ejecutar)");
        System.out.println("  lsc --version           Muestra la versión");
        System.out.println("  lsc help                Muestra esta ayuda");
    }
}
