# LogicScript — extensión VS Code / Cursor

Resaltado de sintaxis y **snippets** para archivos `.ls`.

## Instalación (desarrollo local)

1. Abre VS Code o Cursor.
2. **Ctrl+Shift+P** → **Developer: Install Extension from Location...**
3. Selecciona la carpeta `extensions/logicscript` de este repositorio.
4. **Recarga la ventana** (importante tras actualizar snippets).

## Snippets (escribe el prefijo + Tab)

| Prefijo | Inserta |
|---------|---------|
| `lsprog` / `programa` | `module` + `use core` (module = nombre del archivo) |
| `use` | `use core` |
| `atom` | `atom p = "etiqueta"` |
| `nl` | `nl nombre = "texto"` |
| `lettr` / `translate` | `let f = translate(variable_nl)` |
| `lettrl` | `let f = translate("literal")` |
| `formula` / `form` | `formula f = ...` |
| `impl` | `formula f = p impl q` |
| `tauto` | tautología `p or not p` + vertauto |
| `contra` | contradicción `p and not p` + vertauto |
| `vertauto` / `va` | modo corto |
| `vax` | `vertauto f { steps table verdict }` |
| `lsnl` | ejercicio completo NL → translate → vertauto |
| `lsform` | ejercicio fórmula simbólica + vertauto |

Documentación completa: `documentacion LogicScript/EXTENSION-VSCODE.md`

## Tareas integradas (opcional)

```powershell
New-Item -ItemType Directory -Force .vscode
Copy-Item tooling/vscode/tasks.json .vscode/
Copy-Item tooling/vscode/settings.json.example .vscode/settings.json
```

| Tarea | Acción |
|-------|--------|
| LogicScript: check archivo actual | `lsc check` |
| LogicScript: run archivo actual | `lsc run` |
| LogicScript: compilar proyecto | `compile.ps1` |

Requisito: `.\compile.ps1` ejecutado al menos una vez.
