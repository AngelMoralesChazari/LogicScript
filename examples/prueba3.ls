# Contradicción p ∧ ¬p
# Ejecutar: java -cp out logicscript.ls.LscCli run examples/prueba3.ls

module prueba3

use core

atom p = "llover"

formula f = p and not p 

vertauto f