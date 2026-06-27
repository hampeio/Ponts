$ErrorActionPreference = "Stop"

$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $javac) {
    $fallback = "C:\Program Files\JetBrains\PyCharm 2025.2.1.1\jbr\bin\javac.exe"
    if (Test-Path $fallback) {
        $javac = $fallback
    }
}
if (-not $javac) {
    throw "javac was not found. Install Java 11+ JDK or add javac to PATH."
}

$sources = Get-ChildItem -Path ponts -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -cp "lib\jbox2d-library-2.2.1.1.jar;lib\flatlaf-2.1.jar" $sources
