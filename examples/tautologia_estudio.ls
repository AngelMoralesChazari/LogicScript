# LogicScript v1.0 — ejemplo canónico: tautología clásica p ∨ ¬p
# Referencia: documentacion LogicScript/Language-v1.0.md
# Estado: especificación; requiere intérprete/compilador .ls (pendiente).
# Ejecución manual equivalente hoy:
#   java -cp out logicscript.vertauto.VertautoCli --expr "p or not p"

module tautologia_estudio

atom p = "estudio"
formula tautologia = p or not p

vertauto tautologia {
    steps
    table
    verdict
}

# Respuesta rápida (modo corto):
# vertauto tautologia
