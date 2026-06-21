# Emulator repair: snapshots, adb, cold boot
. "$PSScriptRoot\android-sdk-env.ps1"

Write-Host "=== Disk C: free space ==="
$drive = Get-PSDrive C -ErrorAction SilentlyContinue
$minFree = 16106127360
if ($drive -and $drive.Free -lt $minFree) {
    Write-Host "WARNING: less than 15 GB free on C:. Free disk space before using emulator."
}

Write-Host ""
Write-Host "=== Stop stuck emulator ==="
Stop-AndroidEmulatorProcesses

Write-Host ""
Write-Host "=== Clear broken snapshots (Medium_Phone) ==="
Repair-AvdSnapshots -AvdName 'Medium_Phone'

Write-Host ""
Write-Host "=== Reset ADB ==="
try {
    Set-AndroidSdkEnv | Out-Null
    Reset-AdbServer
    adb devices
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

Write-Host ""
Write-Host "Starting AVD cold boot (wait 2-4 min for Android home screen)..."
try {
    Start-FirstAvdIfNeeded -ColdBoot
    Write-Host ""
    Write-Host "OK:"
    adb devices
    Write-Host ""
    Write-Host "Next: cd mobile; .\start-expo.ps1 then press a"
} catch {
    Write-Error $_.Exception.Message
    Write-Host ""
    Write-Host "Manual fix in Android Studio Device Manager:"
    Write-Host "  Medium_Phone -> Wipe Data"
    Write-Host "  Or create NEW AVD: Pixel 6, API 34, Google Play, name LoL_API34"
    Write-Host "  Do NOT use Medium_Phone (API 37 is unstable)"
    Write-Host '  Then: $env:EXPO_ANDROID_AVD = "LoL_API34"'
    exit 1
}
