# Mapa de Puertos de Microservicios

Este documento contiene la lista de puertos en los que corre cada microservicio del ecosistema de la Tienda.
Para acceder a la documentación interactiva (Swagger) y paneles de control (Eureka/RabbitMQ), por favor revisa [Rutas_Acceso_Herramientas.md](Rutas_Acceso_Herramientas.md).

| Microservicio    | Puerto | Propósito Principal |
|------------------|--------|---------------------|
| **Eureka**       | `8761` | Servidor de descubrimiento de servicios. Todos los microservicios se registran aquí. |
| **Api Gateway**  | `8080` | Punto de entrada único para el frontend y validación de seguridad (Token Bearer). |
| **Auth**         | `8081` | Gestión de registro, login, usuarios y generación de JWT. |
| **Catalogo**     | `8082` | Visualización de productos y descripciones. |
| **Inventario**   | `8083` | Gestión de stock y bodegas (Fail Fast en compras). |
| **Carrito**      | `8084` | Gestión temporal de los items que el usuario desea comprar. |
| **Pagos**        | `8085` | Integración simulada con pasarela de tarjetas de crédito. |
| **Pedidos**      | `8086` | Flujo de Checkout, SAGA Orchestrator y guardado del estado del pedido. |
| **Logística**    | `8088` | Gestión de guías de despacho, rutas y aplicación para choferes. |
| **Reportes**     | `8089` | Panel analítico para administradores (ventas, ingresos, etc.). |
| **Notificaciones** | `8090` | Centro de recepción de eventos (RabbitMQ) y envío de alertas a usuarios. |

---

### Puertos de Infraestructura Base
* **MySQL:** `3306` (Base de datos relacional para todos los microservicios)
* **RabbitMQ:** `5672` (Cola de mensajería asíncrona) / `15672` (Panel de Administración de RabbitMQ)
