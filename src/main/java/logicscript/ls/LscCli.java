package logicscript.ls;

public final class LscCli {

    private LscCli() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            imprimirAyuda();
            System.exit(1);
        }
        String comando = args[0];
        switch (comando) {
            case "run" -> {
                if (args.length < 2) {
                    System.err.println("Uso: lsc run <archivo.ls>");
                    System.exit(1);
                }
                try {
                    new LsInterpreter().ejecutarArchivo(args[1]);
                } catch (LsParseException | LsRuntimeException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
            case "help", "--help", "-h" -> imprimirAyuda();
            default -> {
                System.err.println("Comando desconocido: " + comando);
                imprimirAyuda();
                System.exit(1);
            }
        }
    }

    private static void imprimirAyuda() {
        System.out.println("LogicScript compiler / intérprete (lsc)");
        System.out.println();
        System.out.println("Uso:");
        System.out.println("  lsc run <archivo.ls>   Ejecuta un programa .ls");
        System.out.println("  lsc help               Muestra esta ayuda");
    }
}
