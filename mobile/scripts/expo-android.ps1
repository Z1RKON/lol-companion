# Expo on Android emulator (localhost + adb reverse)
. "$PSScriptRoot\android-sdk-env.ps1"

try {
    Set-AndroidSdkEnv | Out-Null
    Start-FirstAvdIfNeeded
    Setup-AndroidDevTunnels
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

Write-Host "ADB:"
adb devices

Set-Location $PSScriptRoot\..
$env:REACT_NATIVE_PACKAGER_HOSTNAME = "127.0.0.1"
npx expo start --android --localhost
