# Локальный запуск бэкенда с переменными из .env (Gradle, без Maven)
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Error "Создайте .env из .env.example и укажите RIOT_API_KEY"
    exit 1
}

function Find-Jdk17 {
    $script:foundJdk = $null
    $candidates = @(
        $env:JAVA_HOME,
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Microsoft\jdk-17*",
        "C:\Program Files\Java\jdk-17*",
        "C:\Program Files\Java\jdk17*"
    )

    foreach ($pattern in $candidates) {
        if (-not $pattern) { continue }
        if (Test-Path $pattern -PathType Container) {
            $javaExe = Join-Path $pattern "bin\java.exe"
            if (Test-Path $javaExe) { return $pattern }
        }
        Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $javaExe = Join-Path $_.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $script:foundJdk = $_.FullName
            }
        }
        if ($script:foundJdk) { return $script:foundJdk }
    }
    return $null
}

$jdk17 = Find-Jdk17
if ($jdk17) {
    $env:JAVA_HOME = $jdk17
    $env:Path = "$jdk17\bin;" + $env:Path
    Write-Host "JAVA_HOME: $jdk17"
    & "$jdk17\bin\java.exe" -version
} else {
    Write-Error @"
JDK 17 не найден. Spring Boot 3.3 требует Java 17 (у вас может быть только Java 8).

Установите JDK 17:
  winget install Microsoft.OpenJDK.17

Или скачайте: https://adoptium.net/temurin/releases/?version=17

После установки перезапустите PowerShell и снова запустите этот скрипт.
"@
    exit 1
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
        Write-Host "OK: $name"
    }
}

Write-Host "Запуск Spring Boot (Gradle)..."
Set-Location $PSScriptRoot
& .\gradlew.bat bootRun
