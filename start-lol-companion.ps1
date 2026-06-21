# LoL Companion - one-click launcher (backend + emulator + mobile app)
$ErrorActionPreference = 'Stop'

$ProjectRoot = $PSScriptRoot
$BackendScript = Join-Path $ProjectRoot 'backend\start-with-env.ps1'
$MobileDir = Join-Path $ProjectRoot 'mobile'
$AndroidScript = Join-Path $MobileDir 'scripts\run-android-native.ps1'

if (-not (Test-Path $BackendScript)) {
    Write-Error "Backend script not found: $BackendScript"
    exit 1
}
if (-not (Test-Path $AndroidScript)) {
    Write-Error "Mobile script not found: $AndroidScript"
    exit 1
}

function Test-BackendPort {
    try {
        return (Test-NetConnection -ComputerName '127.0.0.1' -Port 8080 -WarningAction SilentlyContinue).TcpTestSucceeded
    } catch {
        return $false
    }
}

function Wait-Backend {
    param([int]$TimeoutSeconds = 180)

    Write-Host "Waiting for backend on http://localhost:8080 ..."
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-BackendPort) {
            Write-Host "Backend is ready."
            return $true
        }
        Start-Sleep -Seconds 3
        Write-Host "  still starting..."
    }
    return $false
}

Write-Host "=== LoL Companion ==="
Write-Host ""

if (Test-BackendPort) {
    Write-Host "Backend already running on port 8080."
} else {
    Write-Host "Starting backend in a new window..."
    Start-Process powershell.exe -ArgumentList @(
        '-NoExit',
        '-NoProfile',
        '-ExecutionPolicy', 'Bypass',
        '-File', $BackendScript
    ) -WorkingDirectory (Split-Path $BackendScript -Parent)

    if (-not (Wait-Backend)) {
        Write-Error "Backend did not start in time. Check the backend window for errors (PostgreSQL, .env, JDK 17)."
        exit 1
    }
}

Write-Host ""
Write-Host "Starting emulator and mobile app..."
Set-Location $MobileDir
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $AndroidScript
exit $LASTEXITCODE
