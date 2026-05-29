# Arquitectura del Sistema de E-Commerce (Microservicios)

Este documento describe la arquitectura general del proyecto, el cual está compuesto por **11 microservicios (aplicaciones Spring Boot)**, diseñados bajo el patrón CSR (Controller-Service-Repository) y desplegados en puertos independientes.

El sistema cumple rigurosamente con los principios de microservicios:
* **Desacoplamiento total:** Cada microservicio es un proyecto Maven independiente.
* **Persistencia aislada:** Cada servicio cuenta con su propia base de datos (MySQL 8.0). Ninguna tabla es compartida.
* **Comunicación Inteligente:** Uso combinado de OpenFeign (Síncrono) y RabbitMQ (Asíncrono).
* **Seguridad Centralizada:** Autenticación vía JWT filtrada a través del API Gateway.

---

## Ecosistema de Microservicios (11 Módulos)

### 1. Infraestructura Base
1. **Eureka Server (`8761`)**: Servidor de descubrimiento. Permite que los microservicios se encuentren entre sí de forma dinámica sin quemar IPs en el código.
2. **API Gateway (`8080`)**: Puerta de entrada única. Contiene el `AuthFilter` que intercepta todas las peticiones, valida la firma criptográfica del JWT y extrae los roles para inyectarlos en las cabeceras internas.

### 2. Módulos de Negocio Core (Operativa Directa)
3. **Auth / Perfiles (`8081`)**: Gestiona la autenticación, registro de usuarios, encriptación de contraseñas (BCrypt) y administración de los Perfiles de Usuario (CRUD de direcciones de entrega).
4. **Catálogo (`8082`)**: Microservicio público. Expone el listado de productos, precios y su stock visual para que los clientes puedan navegar rápidamente.
5. **Inventario (`8083`)**: Microservicio privado (Back-office). Gestiona bodegas físicas y existencias reales. Implementa el patrón *Fail Fast*: si no hay stock físico, rechaza la transacción antes del cobro.
6. **Carrito (`8084`)**: Almacenamiento temporal y ágil de la intención de compra del usuario.

### 3. Orquestación y Financiero
7. **Pedidos (`8086`)**: El corazón del sistema. Actúa como Orquestador SAGA. Cuando recibe un checkout, contacta a Inventario, luego a Pagos y finalmente a Carrito. También almacena el historial detallado de compras (`ItemPedido`) y sincroniza el estado real de la entrega.
8. **Pagos (`8085`)**: Simulador de pasarela financiera. Cobra y emite reembolsos, notificando al resto del sistema vía RabbitMQ.

### 4. Operaciones Post-Venta
9. **Logística (`8088`)**: Gestiona la asignación de pedidos a los choferes, cálculo de rutas y actualizaciones de estado de entrega (Entregado, Cancelado).
10. **Notificaciones (`8090`)**: Consumidor RabbitMQ. Escucha los eventos globales del sistema y genera alertas/mensajes dirigidos a los usuarios.
11. **Reportes / Analytics (`8089`)**: Módulo gerencial. Consume datos transaccionales para calcular métricas en tiempo real (ej. Producto Más Vendido, Ingresos Totales).

---

## Patrones Arquitectónicos Aplicados

*   **API Gateway Pattern**: Centraliza la seguridad y enrutamiento.
*   **Service Discovery Pattern**: Eureka para escalabilidad horizontal.
*   **Database per Service Pattern**: Aislamiento estricto de los datos.
*   **SAGA Orchestration Pattern**: En el proceso de Checkout, `Pedidos` coordina las transacciones distribuidas.
*   **Event-Driven Architecture (EDA)**: Uso de RabbitMQ para procesos asíncronos (Sincronización de estados, notificaciones y reembolsos).
*   **CQRS (Simulado)**: Separación conceptual de Catálogo (Lectura rápida) vs Inventario (Escritura transaccional pesada).
