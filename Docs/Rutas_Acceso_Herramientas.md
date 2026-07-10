# Rutas de Acceso a Herramientas e Interfaces

Esta guía detalla las URLs directas para acceder a las interfaces gráficas y documentación interactiva del ecosistema de microservicios.

## 1. Servidor de Descubrimiento (Eureka)
- **Panel de Administración Eureka:** [http://localhost:8761/](http://localhost:8761/)
  > [!TIP]
  > Aquí puedes visualizar en tiempo real todos los microservicios que se han registrado exitosamente.

## 2. API Gateway
- **Ruta Base de la Tienda:** `http://localhost:8080/`
  > [!NOTE]
  > Todas las peticiones de los clientes (Postman, Frontend, Apps) pasan por aquí. El API Gateway hace de proxy reverso hacia los microservicios subyacentes.

## 3. RabbitMQ (Colas de Mensajería Asíncrona)
- **Panel de Administración (Management):** [http://localhost:15672/](http://localhost:15672/)
- **Credenciales por Defecto:** 
  - Usuario: `user`
  - Contraseña: `password`

---

## 4. Swagger UI (Documentación Interactiva de Microservicios)
Cada microservicio expone su propia documentación gráfica generada mediante OpenAPI v3. Las políticas de seguridad (`SecurityConfig`) permiten acceder libremente a estas rutas para facilitar las pruebas.

| Microservicio | Puerto | Swagger UI URL | OpenAPI JSON |
|---|---|---|---|
| **Auth** | `8081` | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) | [http://localhost:8081/auth/v3/api-docs](http://localhost:8081/auth/v3/api-docs) |
| **Catalogo** | `8082` | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) | [http://localhost:8082/api/productos/v3/api-docs](http://localhost:8082/api/productos/v3/api-docs) |
| **Inventario** | `8083` | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) | [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs) |
| **Carrito** | `8084` | [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) | [http://localhost:8084/api/carrito/v3/api-docs](http://localhost:8084/api/carrito/v3/api-docs) |
| **Pagos** | `8085` | [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) | [http://localhost:8085/api/pagos/v3/api-docs](http://localhost:8085/api/pagos/v3/api-docs) |
| **Pedidos** | `8086` | [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html) | [http://localhost:8086/api/pedidos/v3/api-docs](http://localhost:8086/api/pedidos/v3/api-docs) |
| **Logística** | `8088` | [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html) | [http://localhost:8088/api/logistica/v3/api-docs](http://localhost:8088/api/logistica/v3/api-docs) |
| **Reportes** | `8089` | [http://localhost:8089/swagger-ui.html](http://localhost:8089/swagger-ui.html) | [http://localhost:8089/api/reportes/v3/api-docs](http://localhost:8089/api/reportes/v3/api-docs) |
| **Notificaciones** | `8090` | [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html) | [http://localhost:8090/api/notificaciones/v3/api-docs](http://localhost:8090/api/notificaciones/v3/api-docs) |

> [!IMPORTANT]
> Dado que la seguridad de Swagger está liberada, no es necesario mandar tokens Bearer para cargar la interfaz web gráficamente. Sin embargo, si deseas **ejecutar** desde Swagger una ruta protegida (ej: `POST /api/carrito/items`), Swagger arrojará 403 a menos que implementes un botón de "Authorize" con el JWT. Se recomienda usar la **Colección de Postman** provista en el repositorio para flujos de seguridad y autenticación.
