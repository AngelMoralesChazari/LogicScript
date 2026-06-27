param(
    [string] $JdkHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"
if (-not $JdkHome -or -not (Test-Path "$JdkHome\bin\javac.exe")) {
    Write-Error "No se encontro javac. Instala JDK 17+ y define JAVA_HOME, o usa: .\compile.ps1 -JdkHome 'C:\Program Files\Java\jdk-25'"
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
    & $javac --release 17 -encoding UTF-8 -d out @files
    $resSrc = Join-Path $root "src\main\resources"
    if (Test-Path $resSrc) {
        Copy-Item -Path (Join-Path $resSrc "*") -Destination (Join-Path $root "out") -Recurse -Force
        Write-Host "Recursos copiados a out\"
    }
    Write-Host "OK: compilado con --release 17 en $root\out"
    Write-Host "Traducir: & `"$java`" -cp out logicscript.LogicScriptCli `"si estudio apruebo`""
    Write-Host "Regresion: & `"$java`" -cp out logicscript.LogicScriptRegressionHarness"
    Write-Host "Vertauto:  & `"$java`" -cp out logicscript.vertauto.VertautoCli --expr `"p or not p`""
    Write-Host "Vertauto NL: & `"$java`" -cp out logicscript.vertauto.VertautoCli --from-nl `"si estudio apruebo`""
    Write-Host "Vertauto test: & `"$java`" -cp out logicscript.vertauto.VertautoHarness"
}
finally {
    Pop-Location
}
