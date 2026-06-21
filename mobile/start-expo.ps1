# Expo + Android SDK (adb reverse + localhost для эмулятора)
. "$PSScriptRoot\scripts\android-sdk-env.ps1"

try {
    Set-AndroidSdkEnv | Out-Null
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

if (-not (Get-AndroidDeviceReady)) {
    Write-Host "Emulator not running. Starting AVD..."
    Write-Host "If snapshot errors: npm run android:fix"
    try {
        Start-FirstAvdIfNeeded
    } catch {
        Write-Error $_.Exception.Message
        exit 1
    }
} else {
    Setup-AndroidDevTunnels
}

Write-Host "ADB:"
adb devices
Write-Host ""
Write-Host "Metro (localhost + adb reverse). After start press: a"
Write-Host "Emulator API URL in .env should be: EXPO_PUBLIC_API_URL=http://10.0.2.2:8080/api"
Write-Host ""

Set-Location $PSScriptRoot
$env:REACT_NATIVE_PACKAGER_HOSTNAME = "127.0.0.1"
npx expo start --localhost
