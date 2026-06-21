# Restore Expo settings.gradle + Windows-safe autolinking (UTF-8 no BOM)
$settingsPath = Join-Path $PSScriptRoot "..\android\settings.gradle"
if (-not (Test-Path $settingsPath)) {
    return
}

$content = @'
pluginManagement {
  def reactNativeGradlePlugin = new File(
    providers.exec {
      workingDir(rootDir)
      commandLine("node", "--print", "require.resolve('@react-native/gradle-plugin/package.json', { paths: [require.resolve('react-native/package.json')] })")
    }.standardOutput.asText.get().trim()
  ).getParentFile().absolutePath
  includeBuild(reactNativeGradlePlugin)

  def expoPluginsPath = new File(
    providers.exec {
      workingDir(rootDir)
      commandLine("node", "--print", "require.resolve('expo-modules-autolinking/package.json', { paths: [require.resolve('expo/package.json')] })")
    }.standardOutput.asText.get().trim(),
    "../android/expo-gradle-plugin"
  ).absolutePath
  includeBuild(expoPluginsPath)
}

plugins {
  id("com.facebook.react.settings")
  id("expo-autolinking-settings")
}

def appRoot = new File(rootDir, "..")
expoAutolinking.projectRoot = appRoot

def autolinkCli = new File(appRoot, "scripts/expo-autolinking-cli.js").absolutePath

extensions.configure(com.facebook.react.ReactSettingsExtension) { ex ->
  if (System.getenv('EXPO_USE_COMMUNITY_AUTOLINKING') == '1') {
    ex.autolinkLibrariesFromCommand()
  } else {
    ex.autolinkLibrariesFromCommand([
      "node",
      autolinkCli,
      "react-native-config",
      "--platform", "android",
      "--json",
      "--project-root", appRoot.absolutePath,
      "--source-dir", rootDir.absolutePath
    ])
  }
}
expoAutolinking.useExpoModules()

rootProject.name = 'LoL Companion'

expoAutolinking.useExpoVersionCatalog()

include ':app'
includeBuild(expoAutolinking.reactNativeGradlePlugin)
'@

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Resolve-Path $settingsPath).Path, $content, $utf8NoBom)
Write-Host "Restored android/settings.gradle (Expo + direct node autolinking, UTF-8 no BOM)"

$wrapperPath = Join-Path $PSScriptRoot "..\android\gradle\wrapper\gradle-wrapper.properties"
if (Test-Path $wrapperPath) {
    $wrapper = Get-Content $wrapperPath -Raw
    $wrapper = $wrapper -replace 'networkTimeout=\d+', 'networkTimeout=600000'
    [System.IO.File]::WriteAllText((Resolve-Path $wrapperPath).Path, $wrapper, $utf8NoBom)
    Write-Host "Patched gradle-wrapper.properties (networkTimeout=600000)"
}
