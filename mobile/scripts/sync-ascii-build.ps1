# Mirror mobile/ to a pure ASCII path (Gradle + RN autolinking break on Cyrillic paths).
param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [string]$DestRoot = 'C:\lol-companion-build'
)

$SourceRoot = (Resolve-Path $SourceRoot).Path

if (-not (Test-Path $DestRoot)) {
    try {
        New-Item -ItemType Directory -Path $DestRoot -Force | Out-Null
    } catch {
        $DestRoot = Join-Path $env:ProgramData 'lol-companion-build'
        New-Item -ItemType Directory -Path $DestRoot -Force | Out-Null
        Write-Host "Using fallback build path: $DestRoot"
    }
}

Write-Host "Syncing source -> $DestRoot"
Write-Host "(node_modules and Gradle caches stay in the ASCII build folder)"

$robocopyArgs = @(
    $SourceRoot,
    $DestRoot,
    '/E',
    '/XD', 'node_modules', 'android\build', 'android\.gradle', '.git', '.expo',
    '/NFL', '/NDL', '/NJH', '/NJS', '/nc', '/ns', '/np'
)

$robocopyExit = 0
& robocopy @robocopyArgs | Out-Null
$robocopyExit = $LASTEXITCODE
# robocopy exit codes 0-7 mean success (files copied or nothing to do)
if ($robocopyExit -gt 7) {
    throw "robocopy failed with exit code $robocopyExit"
}

foreach ($name in @('.env', '.env.local')) {
    $src = Join-Path $SourceRoot $name
    if (Test-Path $src) {
        Copy-Item $src (Join-Path $DestRoot $name) -Force
    }
}

return (Resolve-Path $DestRoot).Path
