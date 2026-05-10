@echo off
title Secuencia de Port Knocking
set HOST=tienda-microservicios.duckdns.org
:: Pon aquí tus 3 puertos exactos separados por comas
set PORTS=220604,28350,42100

echo [!] Golpeando la puerta en %HOST%...
powershell -Command "foreach($p in %PORTS%){ try{ (New-Object System.Net.Sockets.TcpClient).BeginConnect('%HOST%', $p, $null, $null).AsyncWaitHandle.WaitOne(100, $false) } catch{} }"

echo [OK] Secuencia enviada. El puerto de la BD deberia estar abierto.
timeout /t 3