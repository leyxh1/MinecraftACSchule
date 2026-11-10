@echo off
REM ==================================================================
REM  start.bat - startet den SchoolAntiCheat-Testserver (Paper)
REM
REM  Was das Skript macht:
REM   1. Java 25 suchen
REM   2. Paper herunterladen (nur beim ersten Mal)
REM   3. Gehaertete server.properties anlegen (nur beim ersten Mal)
REM   4. Unser Plugin aus build\libs in den plugins-Ordner kopieren
REM   5. EULA abfragen (nur beim ersten Mal)
REM   6. Server starten und nach einem Absturz automatisch neu starten
REM
REM  Alle Server-Dateien (Welt, Logs, ...) landen in testserver\run\
REM  und werden NICHT ins Git hochgeladen (.gitignore).
REM ==================================================================
setlocal EnableDelayedExpansion
title SchoolAntiCheat Testserver

REM Ordner, in dem dieses Skript liegt (endet mit \)
set "HIER=%~dp0"
set "RUN=%HIER%run"
REM Arbeitsspeicher fuer den Server (4 GB reichen fuer ein paar Spieler)
set "RAM=4G"

if not exist "%RUN%" mkdir "%RUN%"

REM ---------- 1) Java 25 finden ----------
call :finde_java
if not defined JAVA (
    echo [FEHLER] Java 25 wurde nicht gefunden.
    echo Installiere es mit:  winget install EclipseAdoptium.Temurin.25.JDK
    pause
    exit /b 1
)
echo [OK] Java: !JAVA!

REM ---------- 2) Paper herunterladen ----------
if not exist "%RUN%\paper.jar" (
    echo [..] Lade Paper herunter ...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%HIER%paper-download.ps1" -Ziel "%RUN%\paper.jar"
    if errorlevel 1 (
        echo [FEHLER] Download fehlgeschlagen.
        pause
        exit /b 1
    )
)

REM ---------- 3) Gehaertete Konfiguration ----------
if not exist "%RUN%\server.properties" (
    copy /y "%HIER%server.properties.vorlage" "%RUN%\server.properties" >nul
    echo [OK] server.properties aus Vorlage erstellt.
)

REM ---------- 4) Unser Plugin kopieren ----------
if not exist "%RUN%\plugins" mkdir "%RUN%\plugins"
set "PLUGIN="
for %%F in ("%HIER%..\build\libs\SchoolAntiCheat-*.jar") do set "PLUGIN=%%F"
if defined PLUGIN (
    copy /y "!PLUGIN!" "%RUN%\plugins\SchoolAntiCheat.jar" >nul
    echo [OK] Plugin kopiert: !PLUGIN!
) else (
    echo [WARNUNG] Kein Plugin gefunden. Erst im Projektordner bauen:  gradlew.bat build
)

REM ---------- 5) EULA ----------
findstr /c:"eula=true" "%RUN%\eula.txt" >nul 2>&1
if errorlevel 1 (
    echo.
    echo Bevor der Server startet, musst du die Minecraft-EULA akzeptieren:
    echo   https://aka.ms/MinecraftEULA
    choice /c JN /m "Hast du die EULA gelesen und akzeptierst du sie"
    if errorlevel 2 (
        echo Ohne EULA kann der Server nicht starten.
        pause
        exit /b 1
    )
    echo eula=true> "%RUN%\eula.txt"
)

REM ---------- 6) Server starten (mit automatischem Neustart) ----------
cd /d "%RUN%"
:schleife
echo.
echo [..] Server startet. Zum Beenden in der Konsole "stop" eingeben.
"%JAVA%" -Xms%RAM% -Xmx%RAM% -jar paper.jar --nogui
echo.
choice /c JN /t 15 /d J /m "Server wurde beendet. Neu starten (automatisch J nach 15 s)"
if errorlevel 2 goto :ende
goto :schleife

:ende
endlocal
exit /b 0

REM ------------------------------------------------------------------
REM  Unterprogramm: sucht ein Java 25 und speichert den Pfad in JAVA
REM ------------------------------------------------------------------
:finde_java
set "JAVA="
REM a) Java im PATH, wenn es Version 25 ist
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JV=%%~V"
if defined JV if "!JV:~0,3!"=="25." set "JAVA=java"
if defined JAVA goto :eof
REM b) Installiertes Temurin 25
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-25*") do if exist "%%D\bin\java.exe" set "JAVA=%%D\bin\java.exe"
if defined JAVA goto :eof
REM c) Das JDK 25, das Gradle fuer den Build heruntergeladen hat
for /d %%D in ("%USERPROFILE%\.gradle\jdks\*-25-*") do if exist "%%D\bin\java.exe" set "JAVA=%%D\bin\java.exe"
goto :eof
