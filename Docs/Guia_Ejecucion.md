# Guía de Ejecución de Microservicios 🚀

Para levantar correctamente toda la infraestructura de la Tienda, sigue estos pasos en orden para evitar errores de conexión o dependencias.

## 1. Requisitos Previos (Infraestructura Base)

Antes de iniciar cualquier aplicación en Spring Boot, los servicios base deben estar activos:

*   **Docker Desktop:** Asegúrate de tener la aplicación de Docker Desktop abierta en tu computadora.
*   **Encender Base de Datos y Colas:** Abre una terminal en la ruta principal del proyecto (`D:\trabajos\Tienda`) y ejecuta el siguiente comando:
    ```bash
    docker-compose up -d
    ```
    *(Este comando leerá el archivo `docker-compose.yml` y levantará automáticamente MySQL en el puerto `3306` y RabbitMQ en el `5672`).*

> [!WARNING]
> Si intentas lanzar los microservicios sin la base de datos o RabbitMQ encendidos, las aplicaciones fallarán y se cerrarán ("Crash").

## 2. Lanzamiento del Ecosistema

He configurado un script automatizado para levantar todo el ecosistema en el orden correcto.

1.  Abre el explorador de archivos y dirígete a la carpeta `D:\trabajos\Tienda\Microservicios\`.
2.  Haz doble clic en el archivo **`Microservicios.bat`**.
3.  Se abrirá una consola principal (Lanzador Maestro) y varias ventanas secundarias. **No las cierres.**

### ¿Qué hace el script por detrás?
El script respeta los tiempos de carga obligatorios:
*   Primero, levanta el **Servidor Eureka** y espera 20 segundos para asegurar que esté listo para recibir registros.
*   Segundo, levanta el **API Gateway** y espera 10 segundos.
*   Tercero, levanta el servicio de **Seguridad (Auth)** y espera 10 segundos.
*   Finalmente, lanza en paralelo todos los servicios de negocio (`Catalogo`, `Inventario`, `Pedidos`, `Pagos`, `Carrito`, `Logistica`, `Reportes`, `Notificaciones`).

## 3. Verificación

Una vez que todas las ventanas han dejado de imprimir logs masivamente:
1.  Abre tu navegador y entra a: **http://localhost:8761**
2.  Verás el panel de control de **Eureka**.
3.  Asegúrate de que en la lista de "Instances currently registered with Eureka" aparezcan todos los microservicios en estado `UP`.
4.  ¡Listo! Puedes ir a Bruno o Postman y realizar todas las peticiones a través del **API Gateway** en `http://localhost:8080`.
