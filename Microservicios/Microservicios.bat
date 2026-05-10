@echo off
title Lanzador Maestro de Microservicios - Spring Boot
color 0b

:: --- CONFIGURACION DE RUTAS (Ajusta la de Eureka si es distinta) ---
set RUTA_EUREKA=C:\Users\Administrator\Documents\Tienda\Microservicios\eureka
set RUTA_GATEWAY=C:\Users\Administrator\Documents\Tienda\Microservicios\apigateway
set RUTA_AUTH=C:\Users\Administrator\Documents\Tienda\Microservicios\auth
set RUTA_CATALOGO=C:\Users\Administrator\Documents\Tienda\Microservicios\catalogo
set RUTA_INVENTARIO=C:\Users\Administrator\Documents\Tienda\Microservicios\inventario
set RUTA_PEDIDOS=C:\Users\Administrator\Documents\Tienda\Microservicios\pedidos
set RUTA_PAGOS=C:\Users\Administrator\Documents\Tienda\Microservicios\pagos

echo ====================================================
echo INICIANDO INFRAESTRUCTURA BASE
echo ====================================================

:: 1. Iniciar Eureka Server
echo Lanzando EUREKA...
start "SERVIDIOR EUREKA" cmd /k "cd /d %RUTA_EUREKA% && mvn spring-boot:run"

:: Espera de 20 segundos para que Eureka cargue completamente
echo Esperando a que Eureka este listo...
timeout /t 20 /nobreak

:: 2. Iniciar API Gateway
echo Lanzando API GATEWAY...
start "GATEWAY" cmd /k "cd /d %RUTA_GATEWAY% && mvn spring-boot:run"

:: Espera de 10 segundos
timeout /t 10 /nobreak

echo ====================================================
echo LANZANDO SERVICIOS DE NEGOCIO
echo ====================================================

:: 3. Iniciar el resto de microservicios en paralelo
echo Lanzando AUTH...
start "AUTH-SERVICE" cmd /k "cd /d %RUTA_AUTH% && mvn spring-boot:run"

echo Lanzando CATALOGO...
start "CATALOGO-SERVICE" cmd /k "cd /d %RUTA_CATALOGO% && mvn spring-boot:run"

echo Lanzando INVENTARIO...
start "INVENTARIO-SERVICE" cmd /k "cd /d %RUTA_INVENTARIO% && mvn spring-boot:run"

echo Lanzando PEDIDOS...
start "PEDIDOS-SERVICE" cmd /k "cd /d %RUTA_PEDIDOS% && mvn spring-boot:run"

echo Lanzando PAGOS...
start "PAGOS-SERVICE" cmd /k "cd /d %RUTA_PAGOS% && mvn spring-boot:run"

echo.
echo ====================================================
echo TODOS LOS SERVICIOS HAN SIDO LANZADOS
echo ====================================================
pause