@echo off
title Lanzador Maestro de Microservicios - Spring Boot
color 0b

:: Obtenemos la ruta actual del archivo .bat (dinamico)
set RUTA_BASE=%~dp0

echo --- INICIANDO INFRAESTRUCTURA ---

:: 1. Iniciar Eureka Server
echo Lanzando EUREKA...
start "SERVIDOR EUREKA" cmd /k "cd /d %RUTA_BASE%eureka && mvnw spring-boot:run"

:: Espera de 20 segundos para que Eureka cargue completamente
echo Esperando a que Eureka este listo...
timeout /t 20 /nobreak

:: 2. Iniciar API Gateway
echo Lanzando API GATEWAY...
start "GATEWAY" cmd /k "cd /d %RUTA_BASE%apigateway && mvnw spring-boot:run"

:: Espera de 10 segundos
timeout /t 10 /nobreak

:: 3. Iniciar Auth
echo Lanzando AUTH (Seguridad)...
start "AUTH-SERVICE" cmd /k "cd /d %RUTA_BASE%auth && mvnw spring-boot:run"

:: Espera de 10 segundos adicionales para seguridad
timeout /t 10 /nobreak

echo --- LANZANDO SERVICIOS DE NEGOCIO EN PARALELO ---

start "CATALOGO-SERVICE" cmd /k "cd /d %RUTA_BASE%catalogo && mvnw spring-boot:run"
start "INVENTARIO-SERVICE" cmd /k "cd /d %RUTA_BASE%inventario && mvnw spring-boot:run"
start "PEDIDOS-SERVICE" cmd /k "cd /d %RUTA_BASE%pedidos && mvnw spring-boot:run"
start "PAGOS-SERVICE" cmd /k "cd /d %RUTA_BASE%pagos && mvnw spring-boot:run"
start "CARRITO-SERVICE" cmd /k "cd /d %RUTA_BASE%carrito && mvnw spring-boot:run"
start "LOGISTICA-SERVICE" cmd /k "cd /d %RUTA_BASE%logistica && mvnw spring-boot:run"
start "REPORTES-SERVICE" cmd /k "cd /d %RUTA_BASE%reportes && mvnw spring-boot:run"

echo TODOS LOS SERVICIOS HAN SIDO ENVIADOS A LANZAMIENTO
pause
