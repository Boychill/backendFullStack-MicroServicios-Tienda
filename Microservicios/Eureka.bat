@echo off
title Lanzador de Microservicio Eureka
:: --- CONFIGURACION DE RUTAS ---
set RUTA_EUREKA=C:\Users\Administrator\Documents\Tienda\Microservicios\eureka

echo Lanzando Eureka Server (Puerto 8761)...
start "EUREKA" cmd /k "cd /d %RUTA_EUREKA% && mvn clean spring-boot:run"

pause