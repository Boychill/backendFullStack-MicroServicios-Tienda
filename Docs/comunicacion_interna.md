# Diagrama Lógico y Patrones de Comunicación Inter-Servicios

Este documento detalla el comportamiento estructural y las mecánicas de transferencia de datos entre los 11 microservicios de la tienda. 

Nuestra arquitectura combina **Comunicación Síncrona (Spring Cloud OpenFeign)** para flujos dependientes del tiempo, y **Comunicación Asíncrona (RabbitMQ)** para procesos en segundo plano y sincronización de estados. Todo apuntalado por un entorno de descubrimiento dinámico (**Eureka Server**).

---

## 1. El Muro de Seguridad: API Gateway y Auth

Toda petición de un cliente (Web o Móvil) impacta primero en el **API Gateway**.

1.  El Gateway recibe la solicitud HTTP y la procesa a través de su `AuthFilter`.
2.  Si la ruta está desprotegida (ej. Login o Catálogo), el Gateway funciona como un Proxy reverso.
3.  Si la ruta es protegida, el Gateway valida criptográficamente el JWT. 
4.  **Cruce de Datos (Header Injection):** El Gateway extrae los *Claims* del Token y crea encabezados incrustados (`X-User-Role` y `X-User-Email`). 
5.  El microservicio final recibe la petición y lee esos encabezados (a través de su propio `RoleFilter`) creyendo que el usuario está válidamente autenticado en su ecosistema.

---

## 2. El Orquestador SAGA: Flujo de Compras (Pedidos)

Cuando un usuario dispara `POST /api/pedidos/checkout`, el microservicio de **Pedidos** asume el rol de "Director de Orquesta" (Patrón Saga), enviando instrucciones a otros nodos a través de **OpenFeign**.

### Ciclo Exacto del Checkout:
1.  **Validación de Carrito:** Pedidos hace un `GET` síncrono al **Carrito**. Extrae el subtotal y los arrays de los productos.
2.  **Registro Inmutable:** Guarda en su base de datos local la orden en estado `PENDIENTE`, normalizando cada producto dentro de la tabla hija `ItemPedido`.
3.  **Deducción de Inventario (Fail Fast):** Llama mediante Feign a **Inventario** (`POST /api/inventario/descuento`). Si no hay stock suficiente en las bodegas físicas, lanza una excepción y aborta todo (Estado `FALLIDO_STOCK`).
4.  **Cobro Monetario:** Llama a **Pagos**. Pagos procesa la transacción y devuelve el ID de aprobación.
5.  **Cierre (Confirmación):** Si todo es exitoso, Pedidos se marca como `PAGADO` y envía una petición asíncrona por **RabbitMQ** para destruir el carrito de ese usuario.

---

## 3. Sincronización de Estados y Trazabilidad (RabbitMQ)

El ciclo de vida post-venta está 100% automatizado gracias a eventos asíncronos en RabbitMQ. **Pedidos** actúa como la única fuente de la verdad para el estado de la compra, pero "escucha" secretamente a los demás.

1. **Gestión Logística:**
   - Un Administrador asigna la orden a un Chofer en el microservicio **Logística**.
   - El Chofer utiliza su App Móvil para cambiar el estado de la entrega a `ENTREGADO`.
   - Logística dispara el evento `logistica.estado.cambiado` en el *Topic Exchange* de RabbitMQ.
   - **Pedidos** intercepta este evento y actualiza silenciosamente su base de datos, pasando el estado del Pedido a `ENTREGADO`.

2. **Gestión de Reembolsos:**
   - Un Administrador emite un reembolso en la pasarela de **Pagos**.
   - Pagos lanza el evento `pagos.reembolso.exitoso`.
   - **Pedidos** lo escucha y actualiza el estado a `REEMBOLSADO`, devolviendo el stock a las bodegas.

El cliente final solo necesita consultar el endpoint `/api/pedidos/mis-pedidos` para ver la trazabilidad exacta de su orden sin tener que interrogar a toda la empresa.

---

## 4. Sistema Proactivo de Alertas y Notificaciones

El microservicio de **Notificaciones** está suscrito a los distintos tópicos de RabbitMQ para informar en tiempo real a los involucrados, sin sobrecargar los procesos síncronos:

1. **Eventos de Logística (`logistica.estado.cambiado` / `logistica.ruta.asignada`):**
   - **`LISTO_PARA_CHOFER`**: Notifica a los administradores que una orden fue empaquetada.
   - **`EN_RUTA` / `ASIGNADO`**: Notifica al usuario final y al chofer responsable, respectivamente.
   - **`CANCELADA`**: Detiene operaciones notificando al chofer (si estaba en trayecto) y al cliente.
2. **Eventos de Reembolso/Devolución (`pedidos.devolucion`):**
   - Avisa al cliente de la devolución exitosa y a los administradores sobre el ingreso (Tipo `DEVOLUCION`) de stock físico a las bodegas.

---

## 5. Extracción de Datos Inteligente (Reportes / Analytics)

El microservicio de **Reportes** es un consumidor analítico. Está diseñado puramente para labores gerenciales (Dashboards).

1.  **Consulta Masiva (OpenFeign):** Reportes hace una llamada síncrona a **Pedidos** y **Auth** para recopilar la información.
2.  **Agregación en Memoria (Java Streams):** Calcula métricas complejas al vuelo. Por ejemplo, extrae el "Producto Más Vendido" iterando históricamente sobre todos los `ItemPedido` de las ventas en estado `PAGADO`.
3.  **Desacoplamiento Total:** Gracias a este modelo analítico pasivo, si el microservicio de Reportes cae, la tienda y las ventas siguen operando de manera perfectamente normal.
