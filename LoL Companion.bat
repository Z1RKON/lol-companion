@echo off
title LoL Companion
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-lol-companion.ps1"
if errorlevel 1 pause
