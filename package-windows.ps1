param(
    [Parameter(Mandatory = $true)]
    [string]$JdkHome,
    [string]$Destination = (Join-Path $PSScriptRoot "dist")
)

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$jpackage = Join-Path $JdkHome "bin\jpackage.exe"
$jar = Join-Path $JdkHome "bin\jar.exe"
if (-not (Test-Path -LiteralPath $jpackage) -or -not (Test-Path -LiteralPath $jar)) {
    throw "JdkHome must contain bin\jpackage.exe and bin\jar.exe"
}

& (Join-Path $projectRoot "compile.ps1")
if ($LASTEXITCODE -ne 0) {
    throw "Compilation or smoke tests failed."
}

$buildRoot = Join-Path $projectRoot "build\windows-package"
$inputDir = Join-Path $buildRoot "input"
$outputDir = Join-Path $buildRoot "output"
if (Test-Path -LiteralPath $buildRoot) {
    Remove-Item -LiteralPath $buildRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

Push-Location $projectRoot
try {
    & $jar --create --file (Join-Path $inputDir "Ponts.jar") --main-class ponts.Main -C $projectRoot ponts
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create application JAR."
    }
} finally {
    Pop-Location
}

Copy-Item -LiteralPath (Join-Path $projectRoot "lib\jbox2d-library-2.2.1.1.jar") -Destination $inputDir
Copy-Item -LiteralPath (Join-Path $projectRoot "lib\flatlaf-2.1.jar") -Destination $inputDir

& $jpackage `
    --type app-image `
    --name "CartoonBridgeEngineer" `
    --app-version "1.0.0" `
    --vendor "hampeio" `
    --input $inputDir `
    --dest $outputDir `
    --main-jar "Ponts.jar" `
    --main-class "ponts.Main" `
    --add-modules "java.desktop,java.prefs"
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed."
}

$appDir = Join-Path $outputDir "CartoonBridgeEngineer"
$appRes = Join-Path $appDir "res"
New-Item -ItemType Directory -Path $appRes -Force | Out-Null
$trackedResources = & git -C $projectRoot ls-files -- "res"
foreach ($relativePath in $trackedResources) {
    $sourcePath = Join-Path $projectRoot $relativePath
    $destinationPath = Join-Path $appDir $relativePath
    New-Item -ItemType Directory -Path (Split-Path $destinationPath -Parent) -Force | Out-Null
    Copy-Item -LiteralPath $sourcePath -Destination $destinationPath
}
Copy-Item -LiteralPath (Join-Path $projectRoot "LICENSE") -Destination $appDir
Copy-Item -LiteralPath (Join-Path $projectRoot "README.md") -Destination $appDir

New-Item -ItemType Directory -Path $Destination -Force | Out-Null
$finalDir = Join-Path $Destination "CartoonBridgeEngineer"
if (Test-Path -LiteralPath $finalDir) {
    Remove-Item -LiteralPath $finalDir -Recurse -Force
}
Move-Item -LiteralPath $appDir -Destination $finalDir
Write-Output $finalDir
