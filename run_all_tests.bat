@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo Ejecutando Pruebas Unitarias para todos los microservicios
echo ========================================================
echo.

set "BASE_DIR=%~dp0Microservicios"

REM Lista de microservicios a testear
set "MICROSERVICIOS=apigateway auth carrito catalogo inventario logistica notificaciones pagos pedidos reportes"

set "ERRORES=0"

for %%m in (%MICROSERVICIOS%) do (
    echo --------------------------------------------------------
    echo Ejecutando pruebas en: %%m
    echo --------------------------------------------------------
    
    if exist "%BASE_DIR%\%%m\pom.xml" (
        cd /d "%BASE_DIR%\%%m"
        call mvn test
        
        if !errorlevel! neq 0 (
            echo [X] Error en las pruebas de %%m
            set /a ERRORES+=1
        ) else (
            echo [OK] Pruebas pasadas en %%m
        )
    ) else (
        echo [!] No se encontro el pom.xml en %%m, omitiendo...
    )
    echo.
)

echo ========================================================
echo FIN DE LA EJECUCION DE PRUEBAS
echo ========================================================
if !ERRORES! neq 0 (
    echo Hubo fallos en !ERRORES! microservicio^(s^). Revisa los logs arriba.
    exit /b 1
) else (
    echo Todas las pruebas pasaron exitosamente en todos los microservicios.
    exit /b 0
)
