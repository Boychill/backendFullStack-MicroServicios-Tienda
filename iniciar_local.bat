@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo Iniciando Backend Full Stack - Microservicios Tienda
echo (Modo Hibrido: Infraestructura en Docker, Apps en Local)
echo ========================================================
echo.

echo [1/4] Levantando infraestructura con Docker (MySQL y RabbitMQ)...
REM Ejecutamos solo mysql y rabbitmq del docker-compose
docker-compose up tienda-mysql tienda-rabbitmq -d
if %errorlevel% neq 0 (
    echo [ERROR] No se pudo iniciar docker-compose. Asegurate de tener Docker Desktop abierto.
    pause
    exit /b 1
)

echo.
echo [2/4] Esperando 15 segundos para asegurar que MySQL y RabbitMQ esten listos...
timeout /t 15 /nobreak > nul

set "BASE_DIR=%~dp0Microservicios"

echo.
echo [3/4] Lanzando Servicios Principales (Eureka y API Gateway) localmente...
echo.

echo Iniciando: Eureka Server
start "Servicio: eureka" cmd /c "cd /d "%BASE_DIR%\eureka" && color 0B && title Microservicio - Eureka && mvn spring-boot:run"
echo Esperando 20 segundos para que Eureka Server este 100%% operativo...
timeout /t 20 /nobreak > nul

echo Iniciando: API Gateway
start "Servicio: apigateway" cmd /c "cd /d "%BASE_DIR%\apigateway" && color 0D && title Microservicio - API Gateway && mvn spring-boot:run"
echo Esperando 15 segundos para que API Gateway se registre en Eureka...
timeout /t 15 /nobreak > nul

echo.
echo [4/4] Lanzando el resto de los microservicios de negocio localmente...
set "MICROSERVICIOS=auth carrito catalogo inventario logistica notificaciones pagos pedidos reportes"

for %%m in (%MICROSERVICIOS%) do (
    if exist "%BASE_DIR%\%%m\pom.xml" (
        echo Iniciando: %%m
        REM Inicia una nueva ventana de consola para cada microservicio
        start "Servicio: %%m" cmd /c "cd /d "%BASE_DIR%\%%m" && color 0A && title Microservicio - %%m && mvn spring-boot:run"
        REM Espera 5 segundos entre cada lanzamiento para no saturar el procesador
        timeout /t 5 /nobreak > nul
    ) else (
        echo [!] No se encontro %%m, saltando...
    )
)

echo.
echo ========================================================
echo ¡Despliegue local completado exitosamente!
echo.
echo Se han abierto ventanas de consola para cada microservicio.
echo.
echo Para revisar el registro de Eureka ve a:
echo http://localhost:8761
echo.
echo Para probar el sistema ve a Swagger Centralizado:
echo http://localhost:8080/swagger-ui.html
echo ========================================================
pause
