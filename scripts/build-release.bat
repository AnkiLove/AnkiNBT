@echo off
setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Zulu\zulu-21
set HTTPS_PROXY=http://127.0.0.1:7897
set HTTP_PROXY=http://127.0.0.1:7897
set JAVA_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7897 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7897

set SCRIPT_DIR=%~dp0
for %%I in ("%SCRIPT_DIR%..") do set ROOT=%%~fI
pushd %ROOT%

set OK=0
set FAIL=0
set TOTAL=0
set MOD_VER=1.2.0

echo ========================================
echo  AnkiNBT Cross-Version Release Build
echo ========================================

if exist output rmdir /s /q output
mkdir output
mkdir output\NeoForge
mkdir output\Fabric

echo.
echo [NeoForge]
for %%V in (1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11) do (
    echo.
    echo --- Building NeoForge MC %%V ---
    pushd versions\%%V
    call gradlew.bat clean build
    if errorlevel 1 (
        echo [FAILED] NeoForge MC %%V
        set /a FAIL+=1
    ) else (
        echo [OK] NeoForge MC %%V
        set /a OK+=1
    )
    set /a TOTAL+=1
    popd
)

echo.
echo [Fabric]
for %%V in (1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11) do (
    echo.
    echo --- Building Fabric MC %%V ---
    if "%%V"=="1.21" (
        pushd fabric-versions\1.21
    ) else if "%%V"=="1.21.1" (
        pushd fabric
    ) else (
        pushd fabric-versions\%%V
    )
    call gradlew.bat clean build
    if errorlevel 1 (
        echo [FAILED] Fabric MC %%V
        set /a FAIL+=1
    ) else (
        echo [OK] Fabric MC %%V
        set /a OK+=1
    )
    set /a TOTAL+=1
    popd
)

if %FAIL%==0 (
    echo.
    echo [Collecting release jars]
    for %%F in (versions\1.21\build\libs\AnkiNBT-NeoForge-mc1.21-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.1\build\libs\AnkiNBT-NeoForge-mc1.21.1-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.1-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.2\build\libs\AnkiNBT-NeoForge-mc1.21.2-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.2-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.3\build\libs\AnkiNBT-NeoForge-mc1.21.3-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.3-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.4\build\libs\AnkiNBT-NeoForge-mc1.21.4-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.4-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.5\build\libs\AnkiNBT-NeoForge-mc1.21.5-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.5-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.6\build\libs\AnkiNBT-NeoForge-mc1.21.6-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.6-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.7\build\libs\AnkiNBT-NeoForge-mc1.21.7-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.7-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.8\build\libs\AnkiNBT-NeoForge-mc1.21.8-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.8-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.9\build\libs\AnkiNBT-NeoForge-mc1.21.9-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.9-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.10\build\libs\AnkiNBT-NeoForge-mc1.21.10-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.10-%MOD_VER%.jar" >nul
    for %%F in (versions\1.21.11\build\libs\AnkiNBT-NeoForge-mc1.21.11-*.jar) do copy "%%~fF" "output\NeoForge\AnkiNBT-NeoForge-mc1.21.11-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21\build\libs\AnkiNBT-Fabric-mc1.21-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21-%MOD_VER%.jar" >nul
    for %%F in (fabric\build\libs\AnkiNBT-Fabric-mc1.21.1-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.1-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.2\build\libs\AnkiNBT-Fabric-mc1.21.2-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.2-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.3\build\libs\AnkiNBT-Fabric-mc1.21.3-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.3-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.4\build\libs\AnkiNBT-Fabric-mc1.21.4-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.4-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.5\build\libs\AnkiNBT-Fabric-mc1.21.5-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.5-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.6\build\libs\AnkiNBT-Fabric-mc1.21.6-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.6-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.7\build\libs\AnkiNBT-Fabric-mc1.21.7-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.7-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.8\build\libs\AnkiNBT-Fabric-mc1.21.8-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.8-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.9\build\libs\AnkiNBT-Fabric-mc1.21.9-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.9-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.10\build\libs\AnkiNBT-Fabric-mc1.21.10-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.10-%MOD_VER%.jar" >nul
    for %%F in (fabric-versions\1.21.11\build\libs\AnkiNBT-Fabric-mc1.21.11-*.jar) do copy "%%~fF" "output\Fabric\AnkiNBT-Fabric-mc1.21.11-%MOD_VER%.jar" >nul
)

echo.
echo ========================================
echo  Results: Total: %TOTAL% ^| OK: %OK% ^| Failed: %FAIL%
echo ========================================
if %FAIL%==0 (
    echo Release output: %CD%\output
    echo NeoForge jars: %CD%\output\NeoForge
    echo Fabric jars: %CD%\output\Fabric
)
popd
endlocal

pause
