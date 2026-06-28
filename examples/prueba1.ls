# NL → translate → vertauto
# Ejecutar: java -cp out logicscript.ls.LscCli run examples/prueba1.ls

module prueba1

use core

atom p = "Escuchar musica"
atom q = "trabajar"

nl proposicion = "trabajo si escucho musica" 
let traduccion = translate(proposicion)

vertauto traduccion {
    steps
    table
    verdict
}