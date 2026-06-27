$ErrorActionPreference = "Stop"

.\compile.ps1

$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) {
    $fallback = "C:\Program Files\JetBrains\PyCharm 2025.2.1.1\jbr\bin\java.exe"
    if (Test-Path $fallback) {
        $java = $fallback
    }
}
if (-not $java) {
    throw "java was not found. Install Java 11+ JDK/JRE or add java to PATH."
}

& $java -cp ".;lib\jbox2d-library-2.2.1.1.jar;lib\flatlaf-2.1.jar" ponts.Main
