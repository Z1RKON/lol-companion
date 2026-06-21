# Android SDK, adb, emulator from command line (no Android Studio UI).
function Get-AndroidSdkShortPath {
    param([string]$SdkPath = (Join-Path $env:LOCALAPPDATA 'Android\Sdk'))

    if (Test-Path 'C:\Android\Sdk\platform-tools\adb.exe') {
        return 'C:\Android\Sdk'
    }

    if (-not (Test-Path $SdkPath)) {
        throw "Android SDK not found: $SdkPath"
    }

    $fso = New-Object -ComObject Scripting.FileSystemObject
    return $fso.GetFolder($SdkPath).ShortPath
}

function Set-AndroidSdkEnv {
    $sdkReal = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    $sdk = Get-AndroidSdkShortPath -SdkPath $sdkReal

    $env:ANDROID_HOME = $sdk
    $env:ANDROID_SDK_ROOT = $sdk
    $env:Path = "$sdk\platform-tools;$sdk\emulator;$env:Path"
    return $sdk
}

function Get-AndroidDeviceReady {
    return [bool](adb devices | Select-String 'emulator-\d+\s+device$|^\S+\s+device$')
}

function Wait-AndroidDeviceReady {
    param([int]$TimeoutMinutes = 5)

    $deadline = (Get-Date).AddMinutes($TimeoutMinutes)
    $lastLog = Get-Date
    while ((Get-Date) -lt $deadline) {
        if (Get-AndroidDeviceReady) { return }
        if (((Get-Date) - $lastLog).TotalSeconds -ge 15) {
            Write-Host '  waiting for emulator (status device)...'
            adb devices
            $lastLog = Get-Date
        }
        adb reconnect offline 2>$null | Out-Null
        Start-Sleep -Seconds 3
    }

    throw @"
Emulator not ready after $TimeoutMinutes min.
Run: npm run android:fix
Or: `$env:EXPO_ANDROID_AVD = 'LoL_Emu'; npm run android
"@
}

function Stop-AndroidEmulatorProcesses {
    Get-Process -Name 'qemu-system-x86_64', 'emulator', 'emulator64-x86' -ErrorAction SilentlyContinue |
        Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

function Select-PreferredAvd {
    param([string[]]$Avds)

    if ($env:EXPO_ANDROID_AVD -and ($Avds -contains $env:EXPO_ANDROID_AVD)) {
        return $env:EXPO_ANDROID_AVD
    }
    foreach ($preferred in @('LoL_Emu', 'LoL_API34', 'Pixel_6', 'Pixel_6a', 'Pixel_5')) {
        if ($Avds -contains $preferred) { return $preferred }
    }
    return $Avds[0]
}

function Reset-AdbServer {
    adb kill-server 2>$null | Out-Null
    Start-Sleep -Seconds 1
    adb start-server | Out-Null
}

function Setup-AndroidDevTunnels {
    if (-not (Get-AndroidDeviceReady)) { return }
    adb reverse tcp:8081 tcp:8081 2>$null | Out-Null
    adb reverse tcp:8080 tcp:8080 2>$null | Out-Null
    Write-Host 'adb reverse: 8081 (Metro), 8080 (backend API)'
}

function Start-FirstAvdIfNeeded {
    param([switch]$ColdBoot)

    if (Get-AndroidDeviceReady) {
        Setup-AndroidDevTunnels
        return
    }

    $sdk = $env:ANDROID_HOME
    if (-not $sdk) {
        $sdk = Get-AndroidSdkShortPath
    }
    $avds = @(& "$sdk\emulator\emulator.exe" -list-avds)
    if ($avds.Count -eq 0) {
        throw 'No AVD found. Run npm run android once to create LoL_Emu.'
    }

    $avd = Select-PreferredAvd -Avds $avds
    $emuArgs = @('-avd', $avd, '-no-snapshot-load', '-no-snapshot-save', '-gpu', 'swiftshader_indirect')
    if ($ColdBoot) {
        Write-Host 'Cold boot (no snapshots)...'
    }

    Write-Host "Launching: emulator.exe -avd $avd"
    Reset-AdbServer
    Start-Process -FilePath "$sdk\emulator\emulator.exe" -ArgumentList $emuArgs
    Write-Host 'Waiting for boot (up to 4 min)...'
    Wait-AndroidDeviceReady -TimeoutMinutes 4
    Setup-AndroidDevTunnels
}
