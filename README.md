# LogicScript

[![JitPack](https://jitpack.io/v/AngelMoralesChazari/LogicScript.svg)](https://jitpack.io/#AngelMoralesChazari/LogicScript)
[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

Lenguaje de programación **declarativo** y motor simbólico para formalizar un subconjunto controlado de **lenguaje natural (español)** en **fórmulas de lógica proposicional** y evaluar automáticamente sus tablas de verdad.

---

## ⚡ El lenguaje puro (`.ls`) en acción

Escribe tu lógica en un archivo con extensión `.ls` (por ejemplo, `ejercicio.ls`):

```ls
module ejercicio
use core

atom p = "llueve"
atom q = "paraguas"

nl linea = "si llueve llevo paraguas"
let implicacion = translate(linea)

vertauto implicacion {
    steps
    table
    verdict
}
```

### Ejecutar en consola:

```bash
java -jar logicscript.jar run ejercicio.ls
```

### Salida generada:

```text
=== LOGICSCRIPT RUN: ejercicio.ls ===
[TRANSLATE] linea -> (p -> q)
  Pasos:
    1. Entrada normalizada: 'si llueve llevo paraguas'
    2. Tokenizado: 5 tokens
    3. Patron coincidente: condicional_estandar
    4. Formulas proposicionales detectadas:
       - llueve -> p
       - paraguas -> q
    5. IR construido: ImpExpr
    6. Formula emitida: (p -> q)
[VERTAUTO] implicacion
  Formula: (p -> q)
  Proposiciones:
    p: llueve
    q: paraguas
  Tabla de verdad:
    p | q | (p -> q)
    --+---+---------
    V | V |    V    
    V | F |    F    
    F | V |    V    
    F | F |    V    
  Dictamen: CONTINGENCY (filas: 4, verdaderas: 3, falsas: 1)
=== FIN (0 advertencias) ===
```

---

## 🚀 Inicio rápido (Lenguaje Puro)

Si eres estudiante o docente y **solo quieres usar el lenguaje** sin programar en Java:

### 1. Descarga el intérprete
1. Ve a la sección de **[Releases](https://github.com/AngelMoralesChazari/LogicScript/releases)**.
2. Descarga el archivo **`logicscript.jar`**.
3. *(Requiere tener instalado Java 17 o superior).*

### 2. Ejecuta tus programas
* **Ejecutar un script:**
  ```bash
  java -jar logicscript.jar run tu_programa.ls
  ```
* **Verificar sintaxis (sin ejecutar):**
  ```bash
  java -jar logicscript.jar check tu_programa.ls
  ```

### 3. Extensión oficial para Visual Studio Code
Para tener **coloreado de sintaxis**, **snippets** y autocompletado:
1. Descarga `logicscript-0.2.0.vsix` desde los Releases o desde `extensions/logicscript/`.
2. En VS Code, ve a la pestaña **Extensions** (`Ctrl + Shift + X`).
3. Haz clic en el menú de tres puntos `...` arriba a la derecha y selecciona **"Install from VSIX..."**.
4. ¡Listo! Cualquier archivo `.ls` tendrá resaltado nativo.

---

## ☕ Uso como Librería en Java (Para Desarrolladores)

Si eres desarrollador y deseas integrar el motor de LogicScript en tus propias aplicaciones, tutores inteligentes o evaluadores:

### 1. Agregar dependencia (JitPack)

#### En Maven (`pom.xml`):
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.AngelMoralesChazari</groupId>
        <artifactId>LogicScript</artifactId>
        <version>v0.6.1</version>
    </dependency>
</dependencies>
```

#### En Gradle (`build.gradle`):
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.AngelMoralesChazari:LogicScript:v0.6.1'
}
```

---

### 2. Código Java: Traducir lenguaje natural a fórmula

```java
import logicscript.LogicScriptService;
import logicscript.LogicScriptResult;

public class App {
    public static void main(String[] args) {
        LogicScriptService ls = new LogicScriptService();

        LogicScriptResult res = ls.traducir("si estudio apruebo");

        if (res.isExito()) {
            System.out.println("Fórmula: " + res.getFormula()); 
            // Salida: (p → q)
            System.out.println("Proposiciones: " + res.getProposiciones()); 
            // Salida: {estudio=p, apruebo=q}
        } else {
            System.err.println("Error: " + res.getMensaje());
        }
    }
}
```

---

### 3. Código Java: Ejecutar archivos `.ls` completos (Modo Embebido)

También puedes interpretar scripts `.ls` completos directamente desde tu aplicación Java:

```java
import logicscript.ls.LsInterpreter;

public class EvaluadorScript {
    public static void main(String[] args) {
        LsInterpreter interpreter = new LsInterpreter();
        // Ejecuta el archivo e imprime el análisis y tabla de verdad
        interpreter.ejecutarArchivo("ejercicio.ls");
    }
}
```

---

## 🛠️ Compilación desde el código fuente

Si deseas contribuir o modificar el motor:

```powershell
# Usando Maven (genera el JAR en target/)
mvn clean package

# O usando el script nativo de PowerShell
.\compile.ps1 -Release 17
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia **Apache 2.0**. Consulta el archivo `LICENSE` para más detalles.
