# Expo web in browser (no emulator, no Expo Go)
Set-Location $PSScriptRoot
Write-Host "Backend must run: cd backend; .\start-with-env.ps1"
Write-Host "API in .env: EXPO_PUBLIC_API_URL=http://localhost:8080/api"
Write-Host ""
npx expo start --web -c
