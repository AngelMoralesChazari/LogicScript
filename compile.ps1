param(
    [string] $JdkHome = $env:JAVA_HOME,
    [int] $Release = 25
)

$ErrorActionPreference = "Stop"

function Resolve-JdkHome {
    param([string] $Preferred)

    if ($Preferred -and (Test-Path "$Preferred\bin\javac.exe")) {
        return (Resolve-Path $Preferred).Path
    }

    $candidates = @(
        "C:\Program Files\Java\jdk-25.0.2",
        "C:\Program Files\Java\jdk-25",
        "C:\Program Files\Java\latest"
    )

    foreach ($dir in $candidates) {
        if (Test-Path "$dir\bin\javac.exe") {
            return (Resolve-Path $dir).Path
        }
    }

    $jdkDirs = Get-ChildItem "C:\Program Files\Java" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '^jdk-' } |
        Sort-Object Name -Descending

    foreach ($dir in $jdkDirs) {
        if (Test-Path "$($dir.FullName)\bin\javac.exe") {
            return $dir.FullName
        }
    }

    return $null
}

$JdkHome = Resolve-JdkHome -Preferred $JdkHome

if (-not $JdkHome) {
    Write-Error @"
No se encontro javac (JDK $Release+).
Opciones:
  1. Define JAVA_HOME apuntando al JDK (ej. C:\Program Files\Java\jdk-25.0.2)
  2. Ejecuta: .\compile.ps1 -JdkHome 'C:\Program Files\Java\jdk-25.0.2'
"@
}

$javac = Join-Path $JdkHome "bin\javac.exe"
$java = Join-Path $JdkHome "bin\java.exe"

$root = $PSScriptRoot
$srcJava = Join-Path $root "src\main\java"
$files = @()
if (Test-Path $srcJava) {
    $files = Get-ChildItem -Path $srcJava -Recurse -Filter "*.java" -File | ForEach-Object { $_.FullName } | Sort-Object -Unique
}

if ($files.Count -eq 0) {
    Write-Error "No se encontraron fuentes en $root"
}

Push-Location $root
try {
    if (-not (Test-Path "out")) { New-Item -ItemType Directory -Path "out" | Out-Null }
    & $javac --release $Release -encoding UTF-8 -d out @files
    $resSrc = Join-Path $root "src\main\resources"
    if (Test-Path $resSrc) {
        Copy-Item -Path (Join-Path $resSrc "*") -Destination (Join-Path $root "out") -Recurse -Force
        Write-Host "Recursos copiados a out\"
    }
    Write-Host "OK: compilado con JDK en $JdkHome (--release $Release) -> $root\out"
    Write-Host ""
    Write-Host "Comandos de ejemplo (copiar y pegar; no se ejecutan solos):"
    Write-Host "  Traducir:  & `"$java`" -cp out logicscript.LogicScriptCli `"si estudio apruebo`""
    Write-Host "  Regresion: & `"$java`" -cp out logicscript.LogicScriptRegressionHarness"
    Write-Host "  Vertauto:  & `"$java`" -cp out logicscript.vertauto.VertautoCli --expr `"p or not p`""
    Write-Host "  Vertauto NL: & `"$java`" -cp out logicscript.vertauto.VertautoCli --from-nl `"si llueve llevo paraguas`""
    Write-Host "  Tests:     & `"$java`" -cp out logicscript.vertauto.VertautoHarness"
}
finally {
    Pop-Location
}
