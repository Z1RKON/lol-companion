# Creates LoL_Emu AVD (API 36, x86_64) without Android Studio Device Manager.
function Ensure-LolEmulator {
    $avdName = 'LoL_Emu'
    $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    $sysImage = Join-Path $sdk 'system-images\android-36\google_apis_playstore\x86_64'

    if (-not (Test-Path $sysImage)) {
        throw @"
System image not found: $sysImage
Install in SDK Manager: Android 16 (API 36) Google Play Intel x86_64
Or run: sdkmanager `"system-images;android-36;google_apis_playstore;x86_64`"
"@
    }

    $avdRoot = Join-Path $env:USERPROFILE '.android\avd'
    $iniPath = Join-Path $avdRoot "$avdName.ini"
    $avdDir = Join-Path $avdRoot "$avdName.avd"

    if (Test-Path $iniPath) {
        return $avdName
    }

    Write-Host "Creating AVD $avdName (API 36, x86_64, cold boot, software GPU)..."
    New-Item -ItemType Directory -Force -Path $avdDir | Out-Null

    @"
avd.ini.encoding=UTF-8
path=$avdDir
path.rel=avd\$avdName.avd
target=android-36
"@ | Set-Content -Path $iniPath -Encoding UTF8

    @"
AvdId=$avdName
PlayStore.enabled=true
abi.type=x86_64
avd.ini.displayname=LoL Companion Emulator
avd.ini.encoding=UTF-8
disk.dataPartition.size=6G
fastboot.chosenSnapshotFile=
fastboot.forceColdBoot=yes
fastboot.forceFastBoot=no
hw.accelerometer=yes
hw.arc=false
hw.audioInput=yes
hw.battery=yes
hw.cpu.arch=x86_64
hw.cpu.ncore=2
hw.device.name=pixel_5
hw.gpu.enabled=yes
hw.gpu.mode=swiftshader_indirect
hw.keyboard=yes
hw.lcd.density=440
hw.lcd.height=2340
hw.lcd.width=1080
hw.ramSize=2048
hw.sdCard=yes
image.sysdir.1=system-images\android-36\google_apis_playstore\x86_64\
showDeviceFrame=no
tag.display=Google Play
tag.id=google_apis_playstore
target=android-36
vm.heapSize=256
"@ | Set-Content -Path (Join-Path $avdDir 'config.ini') -Encoding UTF8

    Write-Host "AVD $avdName created."
    return $avdName
}
