# LogicScript

Lenguaje de programación **declarativo** para traducir un subconjunto controlado de español a **fórmulas de lógica proposicional**.

LogicScript separa claramente:

- **Datos** — archivos `.lgs` con reglas léxicas (`lemma`, `lexrule`) y semánticas (`pattern`)
- **Motor** — runtime Java que interpreta esas reglas y produce una IR (árbol lógico) y una fórmula simbólica

## Estado actual

| Aspecto | Valor |
|---------|-------|
| Formato de datos | `.lgs` v0.6 |
| Implementación | Java 25 (--release 25), sin dependencias externas |
| Pruebas | `LogicScriptRegressionHarness` (~40 casos) |

## Compilar y ejecutar

```powershell
.\compile.ps1
```

Usa **JDK 25** automáticamente si está en `C:\Program Files\Java\jdk-25.0.2` (o define `JAVA_HOME`).

```powershell
java -cp out logicscript.LogicScriptCli "si estudio apruebo"
java -cp out logicscript.LogicScriptRegressionHarness
```

Si quieres forzar otra ruta de JDK:

```powershell
.\compile.ps1 -JdkHome "C:\Program Files\Java\jdk-25.0.2"
```

Otra versión de bytecode (opcional):

```powershell
.\compile.ps1 -Release 17
```

## Estructura del proyecto

```text
LogicScript/
├── src/main/java/
│   ├── logicscript/     # motor, IR, CLI, vertauto
│   └── nlp/             # lexer, léxico .lgs, semántica
├── src/main/resources/logicscript/core.lgs
├── docs/                # especificación y plan de migración
└── compile.ps1
```

## Documentación

| Archivo | Contenido |
|---------|-----------|
| [docs/FILOSOFIA-Y-OBJETIVO.txt](docs/FILOSOFIA-Y-OBJETIVO.txt) | Visión y filosofía del lenguaje |
| [docs/SINTAXIS-DSL-RESUMEN.md](docs/SINTAXIS-DSL-RESUMEN.md) | Referencia rápida de `.lgs` |
| [docs/spec/LogicScript.md](docs/spec/LogicScript.md) | Libro blanco del pseudolenguaje |
| [docs/INVENTARIO-CLASES-JAVA.md](docs/INVENTARIO-CLASES-JAVA.md) | Mapa del código Java |

## Ejemplo

```
Entrada:  si estudio apruebo
Salida:   (p → q)
```

Donde `p` = estudiar y `q` = aprobar (formas canónicas del léxico).

## Próximos pasos

- Migrar a Maven/Gradle con `groupId` propio
- Migrar a Maven o Gradle
- Evolucionar `.lgs` a v1.0 con directivas `synonym`, `connector`, módulos

Ver [docs/PLAN-MIGRACION-PROYECTO-INDEPENDIENTE.md](docs/PLAN-MIGRACION-PROYECTO-INDEPENDIENTE.md) para el plan completo.
