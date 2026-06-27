package logicscript;

public class LogicScriptService {
    private final LogicScriptEngine engine;

    public LogicScriptService() {
        this.engine = new LogicScriptEngine();
    }

    public LogicScriptResult traducir(String textoNatural) {
        return engine.traducir(textoNatural);
    }
}
