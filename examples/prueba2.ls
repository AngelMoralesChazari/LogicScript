# Estudio o no estudio — tautología p ∨ ¬p
# Ejecutar: java -cp out logicscript.ls.LscCli run examples/prueba2.ls

module prueba2

use core

atom p = "estudiar"

formula f = p or not p

vertauto f