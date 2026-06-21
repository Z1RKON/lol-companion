# Native Android build + CLI emulator (no Android Studio UI required).
. "$PSScriptRoot\android-sdk-env.ps1"
. "$PSScriptRoot\ensure-lol-emulator.ps1"

function Ensure-NodeInPath {
    $node = Get-Command node -ErrorAction SilentlyContinue
    if (-not $node) {
        throw 'Node.js not found in PATH. Install Node.js LTS and reopen the terminal.'
    }
    $nodeDir = Split-Path $node.Source -Parent
    if ($env:Path -notlike "*$nodeDir*") {
        $env:Path = "$nodeDir;$env:Path"
    }
}

function Setup-ProjectGradleEnv {
    param([string]$MobileRoot)

    $gradleHome = Join-Path $MobileRoot '.gradle'
    $tempHome = Join-Path $MobileRoot '.tmp'
    New-Item -ItemType Directory -Force -Path $gradleHome, $tempHome | Out-Null
    $env:GRADLE_USER_HOME = $gradleHome
    $env:TEMP = $tempHome
    $env:TMP = $tempHome
    Write-Host "GRADLE_USER_HOME: $gradleHome"
}

function Seed-GradleWrapperCache {
    param(
        [string]$TargetGradleHome,
        [string]$SourceGradleHome = (Join-Path $env:USERPROFILE '.gradle')
    )

    $gradleVersion = 'gradle-8.14.3-bin'
    $targetDists = Join-Path $TargetGradleHome "wrapper\dists\$gradleVersion"
    if (Get-ChildItem $targetDists -Recurse -Filter 'gradle.bat' -ErrorAction SilentlyContinue | Select-Object -First 1) {
        Write-Host "Gradle $gradleVersion already in project cache."
        return
    }

    $sourceDists = Join-Path $SourceGradleHome "wrapper\dists\$gradleVersion"
    if (-not (Test-Path $sourceDists)) {
        $sourceDists = "C:\lol-companion-build\.gradle\wrapper\dists\$gradleVersion"
    }
    if (-not (Test-Path $sourceDists)) {
        Write-Host "Gradle not found locally - will download (needs stable internet)."
        return
    }

    Write-Host "Copying Gradle $gradleVersion from existing cache (no download)..."
    New-Item -ItemType Directory -Force -Path (Split-Path $targetDists -Parent) | Out-Null
    & robocopy $sourceDists $targetDists /E /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null
    Write-Host 'Gradle wrapper cache ready.'
}

function Stop-StaleGradleDaemons {
    param([string]$AndroidDir, [string]$GradleHome)

    if (-not (Test-Path (Join-Path $AndroidDir 'gradlew.bat'))) { return }
    $prev = $env:GRADLE_USER_HOME
    $env:GRADLE_USER_HOME = $GradleHome
    Push-Location $AndroidDir
    try { .\gradlew.bat --stop 2>$null | Out-Null } finally {
        Pop-Location
        $env:GRADLE_USER_HOME = $prev
    }
}

function Write-AndroidLocalProperties {
    param([string]$AndroidDir, [string]$SdkPath)

    # Pure ASCII path for NDK/CMake (Cyrillic in user profile breaks native build).
    $sdkForProps = if (Test-Path 'C:\Android\Sdk\platform-tools\adb.exe') {
        'C:\Android\Sdk'
    } else {
        $fso = New-Object -ComObject Scripting.FileSystemObject
        $fso.GetFolder($SdkPath).ShortPath
    }
    $sdkDir = $sdkForProps.Replace('\', '\\')
    Set-Content -Path (Join-Path $AndroidDir 'local.properties') -Value "sdk.dir=$sdkDir" -Encoding ASCII
    Write-Host "sdk.dir -> $sdkForProps"
}

try {
    Set-AndroidSdkEnv | Out-Null
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

$sdkPath = $env:ANDROID_HOME
Ensure-NodeInPath

$env:EXPO_ANDROID_AVD = Ensure-LolEmulator

if (-not (Get-AndroidDeviceReady)) {
    Write-Host "Starting emulator from command line ($env:EXPO_ANDROID_AVD)..."
    try {
        Start-FirstAvdIfNeeded -ColdBoot
    } catch {
        Write-Error $_.Exception.Message
        exit 1
    }
} else {
    Setup-AndroidDevTunnels
}

$mobileRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $mobileRoot

if (-not (Test-Path 'node_modules')) {
    Write-Host 'Installing npm dependencies...'
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if (-not (Test-Path 'android')) {
    Write-Host 'First run: expo prebuild...'
    npx expo prebuild --platform android --no-install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

& (Join-Path $PSScriptRoot 'patch-android-gradle.ps1')
Write-AndroidLocalProperties -AndroidDir (Join-Path $mobileRoot 'android') -SdkPath $sdkPath
Setup-ProjectGradleEnv -MobileRoot $mobileRoot
Seed-GradleWrapperCache -TargetGradleHome $env:GRADLE_USER_HOME
Stop-StaleGradleDaemons -AndroidDir (Join-Path $mobileRoot 'android') -GradleHome $env:GRADLE_USER_HOME

# Stale native build cache may keep broken Cyrillic SDK paths.
Remove-Item (Join-Path $mobileRoot 'node_modules\expo-modules-core\android\.cxx') -Recurse -Force -ErrorAction SilentlyContinue

Write-Host 'Building and installing (first time: 10-20 min)...'
$env:REACT_NATIVE_PACKAGER_HOSTNAME = '127.0.0.1'
$env:GRADLE_OPTS = '-Xmx4096m -Dfile.encoding=UTF-8'
npx expo run:android --all-arch
exit $LASTEXITCODE
