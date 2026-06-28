package logicscript.ls.ast;

import java.util.List;

public record LsProgram(String moduleName, List<String> uses, List<LsStmt> statements) {
}
