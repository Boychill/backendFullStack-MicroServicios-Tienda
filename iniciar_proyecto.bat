@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo Iniciando Backend Full Stack - Microservicios Tienda
echo (Despliegue 100%% Contenerizado con Docker)
echo ========================================================
echo.

echo Levantando infraestructura y microservicios.
echo Nota: La primera vez tomara varios minutos mientras Maven compila los .jar internamente.
echo.

docker-compose up --build -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] No se pudo iniciar docker-compose. Asegurate de tener Docker Desktop abierto y configurado.
    pause
    exit /b 1
)

echo.
echo ========================================================
echo ¡Comandos de Docker ejecutados exitosamente!
echo.
echo Docker esta ahora mismo construyendo e iniciando los servicios en segundo plano.
echo.
echo Puedes abrir Docker Desktop para ver los logs en tiempo real o ejecutar:
echo docker-compose logs -f
echo.
echo Cuando todos los servicios esten en verde (Healthy/Started):
echo Para revisar el registro de Eureka ve a:
echo http://localhost:8761
echo.
echo Para probar el sistema ve a Swagger Centralizado:
echo http://localhost:8080/swagger-ui.html
echo ========================================================
pause
