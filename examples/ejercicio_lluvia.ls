# LogicScript v1.0 — ejemplo canónico: implicación desde lenguaje natural
# Guía: documentacion LogicScript/GUIA-RAPIDA-EJERCICIOS.md
# Ejecutar: java -cp out logicscript.ls.LscCli run examples/ejercicio_lluvia.ls

module ejercicio_lluvia

use core

atom p = "llueve"
atom q = "paraguas"

nl linea_lluvia = "si llueve llevo paraguas"
let implicacion = translate(linea_lluvia)

vertauto implicacion {
    steps
    table
    verdict
}
