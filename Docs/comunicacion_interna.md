# Diagrama Lógico y Patrones de Comunicación Inter-Servicios

Este documento detalla el comportamiento estructural, los diagramas de secuencia lógica y las mecánicas de transferencia de datos entre los 8 microservicios de la tienda. 

Nuestra arquitectura utiliza **Comunicación Síncrona (Spring Cloud OpenFeign)** apuntalada por un entorno de descubrimiento dinámico (**Eureka Server**).

---

## 1. El Muro de Seguridad: API Gateway y Auth

Toda petición de un cliente (Web o Móvil) impacta primero en el **API Gateway**.

1.  El Gateway recibe la solicitud HTTP y la procesa a través de su `AuthFilter`.
2.  Si la ruta está desprotegida (ej. `POST /api/auth/login` o `GET /api/productos`), el Gateway funciona como un simple pasadizo (Load Balancer proxy) hacia el destino.
3.  Si la ruta es protegida, el Gateway valida criptográficamente el JWT. 
4.  **Cruce de Datos (Header Injection):** El Gateway extrae los *Claims* del Token y crea encabezados incrustados (`X-User-Role` y `X-User-Email`). 
5.  El microservicio final recibe la petición y lee esos encabezados (a través de su propio `RoleFilter`) creyendo que el usuario está válidamente autenticado en su ecosistema cerrado.

---

## 2. El Orquestador SAGA: Flujo de Compras (Pedidos)

La piedra angular de la tienda es el microservicio de **Pedidos**. Cuando un usuario dispara `POST /api/pedidos/checkout`, este servidor asume el rol de "Director de Orquesta" y detona un Patrón Saga, enviando instrucciones a otros nodos a través de **OpenFeign**.

### 2.1 Sinergia: Carrito, Pagos y Pedidos
El ciclo exacto para procesar una venta segura es:

1.  **Validación de Carrito:** Pedidos hace un `GET` síncrono al **Carrito**. Extrae el subtotal y los arrays de los productos (sus IDs y cantidades deseadas).
2.  **Registro PENDIENTE:** Guarda en su base de datos local la orden en estado `PENDIENTE`.
3.  **Deducción de Inventario (Fail Fast):** Llama mediante Feign a **Inventario** (`POST /api/inventario/descuento`).
    *   *Sinergia Multi-Bodega:* Inventario no solo resta números; busca en qué bodegas físicas hay stock disponible y resta fragmentadamente (Extracción Inteligente). Si los números no alcanzan, lanza una excepción y **Pedidos** aborta todo instantáneamente (Estado `FALLIDO_STOCK`).
4.  **Cobro Monetario:** Pedidos contacta a **Pagos** enviando los datos de la tarjeta. Pagos procesa la transacción de manera aislada con el proveedor bancario (Mock) y devuelve el ID de transacción aprobado.
5.  **Cierre (Confirmación):** Si el pago es exitoso, Pedidos se marca como `PAGADO` y envía una última petición `DELETE` al **Carrito** para destruirlo/vaciarlo en la sesión de ese usuario, completando la compra limpiamente.

---

## 3. Patrón de Espejo Visual: Sincronización de Catálogo

Por diseño estricto, **Catálogo** (lo que ven los clientes) e **Inventario** (lo que controlan los bodegueros) están en bases de datos separadas.

¿Cómo sabe la página web que un producto tiene Stock sin sobrecargar la Base de Datos transaccional de los bodegueros?
A través de un "Espejo Unidireccional".

1.  Un gerente entra a **Inventario** y carga 50 monitores en la *Bodega Norte* y 10 en la *Bodega Central* (`POST /api/inventario/ingreso`).
2.  **Inventario** suma todos los lotes de monitores geográficamente desplegados (Total: 60).
3.  Automáticamente por debajo, Inventario lanza una llamada Feign asumiendo el rol de Sistema (`PUT /api/productos/{id}/stock`) hacia el **Catálogo**.
4.  Catálogo guarda ese número `60` como un campo "visual" de rápida lectura, permitiendo que miles de clientes vean que hay stock disponible en milisegundos sin saturar a los servidores logísticos.

### Ciclo de Vida: Creación y Abastecimiento de un Producto
Para que la sinergia anterior ocurra, el proceso técnico paso a paso es el siguiente:

1. **Nacimiento en el Catálogo:** Un administrador crea el producto base (`POST /api/productos`). Aquí nace con nombre, precio y una variable `stock = 0`. El sistema le asigna un ID único (ej. `Producto ID: 15`). Este ID es la llave maestra para todo el ecosistema.
2. **Alta Física en Inventario:** Los camiones llegan con la mercancía a distintas bodegas. Los encargados no tocan el Catálogo, sino que usan el microservicio de **Inventario** (`POST /api/inventario/ingreso`).
3. **Distribución en Bodegas (`InventarioBodega`):** 
   - El bodeguero informa: "Han llegado 100 unidades del *Producto 15* a la *Bodega A* (ID: 1)".
   - El sistema de Inventario crea un registro relacional en su tabla interna vinculando `bodegaId = 1` y `productoId = 15` con `cantidadDisponible = 100`.
   - Más tarde, otro camión deja 50 unidades en la *Bodega B* (ID: 2). Se crea otro vínculo `bodegaId = 2`, `productoId = 15`, `cantidadDisponible = 50`.
4. **Agrupación y Sincronización:** Cada vez que se registra un ingreso en cualquier bodega, el **Inventario** suma dinámicamente las cantidades de todas las bodegas que tengan ese `productoId` (100 + 50 = 150) y dispara la señal al **Catálogo** para que el cliente final vea "150 disponibles".

---

## 4. El Ecosistema GPS (Logística y Auth)

El siguiente eslabón en el flujo lógico, que tomará la posta tras el Checkout, involucrará a los microservicios de **Logística** y el módulo de perfiles dentro de **Auth**.

*   El microservicio **Auth** (ahora encargado de los Perfiles de Usuario) guarda las libretas de direcciones con Latitud y Longitud dictadas por un pin del mapa.
*   Cuando la SAGA de Pedidos dictamine `PAGADO`, enviará un evento asíncrono o una alerta a **Logística**.
*   **Logística** cruzará llamadas con **Auth** para descubrir *"dónde vive el dueño de ese correo"* y asignará esa orden a la `Ruta` de un Chofer basado en la cercanía GPS para su despacho.

---

## 5. Extracción de Datos Inteligente (Reportes / Analytics)

El microservicio de **Reportes** es un consumidor pasivo. Está diseñado puramente para labores gerenciales y tableros de control (Dashboards).

1.  **Consulta Masiva (OpenFeign):** Reportes hace una llamada `GET` al microservicio de **Pedidos** para traerse todo el historial de ventas.
2.  **Agregación en Memoria:** En lugar de replicar y guardar datos de ventas en una base de datos propia, procesa todo al vuelo utilizando Java Streams.
3.  **Resultado Analítico:** Mapea la información cruda en métricas de negocio (como el ingreso monetario neto de la tienda, la cantidad de operaciones y el desglose de ingresos por fechas).
4.  **Desacoplamiento:** Gracias a este modelo, si el microservicio de Reportes cae, la tienda sigue operando de manera perfectamente normal, pues los clientes reales no dependen de él para comprar.
