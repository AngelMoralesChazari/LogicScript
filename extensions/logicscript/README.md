# LogicScript — extensión VS Code / Cursor

Resaltado de sintaxis para archivos `.ls`.

## Instalación (desarrollo local)

1. Abre VS Code o Cursor.
2. **Extensions** → menú `...` → **Install from VSIX...** no hace falta si usas carpeta local:
3. **Command Palette** (`Ctrl+Shift+P`) → **Developer: Install Extension from Location...**
4. Selecciona la carpeta `extensions/logicscript` de este repositorio.
5. Recarga la ventana si te lo pide.

## Verificar

Abre `examples/ejercicio_lluvia.ls`: deben resaltarse `module`, `vertauto`, `and`, comentarios `#`, cadenas `"..."`.

## Tareas integradas (opcional)

Copia los archivos de `tooling/vscode/` a `.vscode/` en la raíz del proyecto:

```powershell
Copy-Item -Recurse tooling/vscode/* .vscode/
```

Tareas disponibles (`Ctrl+Shift+B` o **Tasks: Run Task**):

| Tarea | Acción |
|-------|--------|
| LogicScript: check archivo actual | `lsc check` sobre el `.ls` abierto |
| LogicScript: run archivo actual | `lsc run` sobre el `.ls` abierto |
| LogicScript: compilar proyecto | `compile.ps1` |

Requisito: haber ejecutado `.\compile.ps1` antes.
