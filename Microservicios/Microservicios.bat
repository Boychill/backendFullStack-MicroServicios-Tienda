@echo off
title Lanzador Maestro de Microservicios - Spring Boot
color 0b

:: Obtenemos la ruta actual del archivo .bat (dinamico)
set RUTA_BASE=%~dp0

echo --- INICIANDO INFRAESTRUCTURA (Docker) ---
echo Levantando MySQL y RabbitMQ con Docker Compose...
cd /d "%RUTA_BASE%.."
docker-compose up -d
echo Esperando 15 segundos para que la Base de Datos y RabbitMQ inicien por completo...
timeout /t 15 /nobreak

echo --- INICIANDO INFRAESTRUCTURA (Spring Boot) ---

:: 1. Iniciar Eureka Server
echo Lanzando EUREKA...
start "SERVIDOR EUREKA" cmd /k "cd /d %RUTA_BASE%eureka && mvnw clean spring-boot:run"

:: Espera de 20 segundos para que Eureka cargue completamente
echo Esperando a que Eureka este listo...
timeout /t 20 /nobreak

:: 2. Iniciar API Gateway
echo Lanzando API GATEWAY...
start "GATEWAY" cmd /k "cd /d %RUTA_BASE%apigateway && mvnw clean spring-boot:run"

:: Espera de 10 segundos
timeout /t 10 /nobreak

:: 3. Iniciar Auth
echo Lanzando AUTH (Seguridad)...
start "AUTH-SERVICE" cmd /k "cd /d %RUTA_BASE%auth && mvnw clean spring-boot:run"

:: Espera de 10 segundos adicionales para seguridad
timeout /t 10 /nobreak

echo --- LANZANDO SERVICIOS DE NEGOCIO EN PARALELO ---

start "CATALOGO-SERVICE" cmd /k "cd /d %RUTA_BASE%catalogo && mvnw clean spring-boot:run"
start "INVENTARIO-SERVICE" cmd /k "cd /d %RUTA_BASE%inventario && mvnw clean spring-boot:run"
start "PEDIDOS-SERVICE" cmd /k "cd /d %RUTA_BASE%pedidos && mvnw clean spring-boot:run"
start "PAGOS-SERVICE" cmd /k "cd /d %RUTA_BASE%pagos && mvnw clean spring-boot:run"
start "CARRITO-SERVICE" cmd /k "cd /d %RUTA_BASE%carrito && mvnw clean spring-boot:run"
start "LOGISTICA-SERVICE" cmd /k "cd /d %RUTA_BASE%logistica && mvnw clean spring-boot:run"
start "REPORTES-SERVICE" cmd /k "cd /d %RUTA_BASE%reportes && mvnw clean spring-boot:run"
start "NOTIFICACIONES-SERVICE" cmd /k "cd /d %RUTA_BASE%notificaciones && mvnw clean spring-boot:run"

echo TODOS LOS SERVICIOS HAN SIDO ENVIADOS A LANZAMIENTO
pause
