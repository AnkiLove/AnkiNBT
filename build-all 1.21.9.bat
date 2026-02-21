@echo off
setlocal enabledelayedexpansion
set JAVA_HOME=C:\Program Files\Zulu\zulu-21
set HTTPS_PROXY=http://127.0.0.1:7897
set HTTP_PROXY=http://127.0.0.1:7897
set JAVA_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7897 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7897

set OK=0
set FAIL=0
set TOTAL=0

echo ========================================
echo  AnkiNBT Multi-Version Build
echo ========================================

echo.
echo [NeoForge]
for %%V in (1.21.9 1.21.10 1.21.11) do (
    echo.
    echo --- Building NeoForge MC %%V ---
    pushd versions\%%V
    call gradlew.bat build
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

echo.
echo --- Building Fabric MC 1.21.1 ---
pushd fabric
call gradlew.bat build
if errorlevel 1 (
    echo [FAILED] Fabric MC 1.21.1
    set /a FAIL+=1
) else (
    echo [OK] Fabric MC 1.21.1
    set /a OK+=1
)
set /a TOTAL+=1
popd

for %%V in (1.21.9 1.21.10 1.21.11) do (
    echo.
    echo --- Building Fabric MC %%V ---
    pushd fabric-versions\%%V
    call gradlew.bat build
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

echo.
echo ========================================
echo  Results: Total: %TOTAL% ^| OK: %OK% ^| Failed: %FAIL%
echo ========================================
endlocal

pause
