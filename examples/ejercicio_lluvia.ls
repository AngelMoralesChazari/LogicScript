# LogicScript v1.0 — ejemplo canónico: implicación desde lenguaje natural
# Referencia: documentacion LogicScript/Language-v1.0.md
# Estado: especificación; requiere intérprete/compilador .ls (pendiente).
# Ejecución manual equivalente hoy:
#   java -cp out logicscript.LogicScriptCli "si llueve llevo paraguas"
#   java -cp out logicscript.vertauto.VertautoCli --from-nl "si llueve llevo paraguas"

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
