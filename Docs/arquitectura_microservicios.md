# Arquitectura de Microservicios - Tienda

Este documento proporciona la descripción de los 10 microservicios que conformarán el sistema de la tienda. Todos los microservicios estarán desarrollados utilizando **Java**, **Maven** y **Spring Boot**.

## Microservicios Definidos

### 1. Servicio de Autenticación (Auth)
**Descripción:** Es responsable de centralizar la seguridad. Maneja el registro de usuarios, el inicio de sesión, la generación y validación de tokens de acceso (por ejemplo, JWT).
**Responsabilidades clave:** Login, validación de credenciales, control de roles y permisos.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-security`, `jjwt-api`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-data-jpa`, Driver SQL (PostgreSQL/MySQL).
**Endpoints Principales:**
- `POST /auth/register`: Registro de un nuevo usuario.
- `POST /auth/login`: Autenticación, validación de credenciales y generación de JWT.
- *(Nota: La validación del token de acceso para las peticiones subsecuentes la realizará directamente el API Gateway).*

### 2. Servicio de Catálogo (Catalogo)
**Descripción:** Gestiona toda la información pública sobre los productos disponibles en la tienda.
**Responsabilidades clave:** Listado de productos, detalles, búsqueda, categorías, marcas e imágenes.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa` (o `spring-boot-starter-data-mongodb`), `spring-cloud-starter-netflix-eureka-client`.
**Endpoints Principales:**
- `GET /productos`: Catálogo principal paginado. Soporta parámetros de búsqueda y filtros (ej. `?categoria=electronica`, `?precioMin=100&precioMax=500`, `?nombre=laptop`).
- `GET /productos/{id}`: Información detallada de un producto específico.
- `POST /productos`: Agregar/crear un nuevo producto en tienda (Ruta protegida para Admin).

### 3. Servicio de Carrito de Compras (Carrito)
**Descripción:** Administra la selección temporal de productos que un usuario desea comprar. Al ser de alta latencia y escritura/lectura constante, suele apoyarse en cachés como Redis.
**Responsabilidades clave:** Agregar/quitar productos, calcular subtotales temporales, vaciar carrito.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign` (para revisar precios con el Catálogo), `spring-cloud-starter-netflix-eureka-client`.
**Endpoints Principales:**
- `GET /carrito/{usuarioId}`: Obtiene el contenido actual de un carrito.
- `POST /carrito/{usuarioId}/agregar`: Añade un ID de producto y cantidad al carrito.
- `DELETE /carrito/{usuarioId}/vaciar`: Elimina todos los elementos del carrito activo.

### 4. Servicio de Pedidos (Pedidos)
**Descripción:** Orquesta el proceso de compra una vez que el usuario confirma el carrito. Crea la orden definitiva y hace seguimiento de su estado ("Pendiente", "Pagado", "Enviado").
**Responsabilidades clave:** Crear orden de compra, historial de pedidos por usuario, gestión del ciclo de vida del pedido.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-openfeign`, `spring-boot-starter-amqp` (RabbitMQ para publicar eventos), `spring-cloud-starter-netflix-eureka-client`.
**Endpoints Principales:**
- `POST /pedidos`: Crea un pedido con estado "Pendiente" partiendo del contenido pagado o a pagar.
- `GET /pedidos/usuario/{usuarioId}`: Historial de compras de un usuario.
- `GET /pedidos/{pedidoId}`: Detalle administrativo del pedido.

### 5. Servicio de Perfil (Perfil)
**Descripción:** Administra la información privada del cliente y sus preferencias.
**Responsabilidades clave:** Datos de contacto, libreta de direcciones de envío o facturación, gestión de la cuenta personal.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-netflix-eureka-client`.
**Endpoints Principales:**
- `GET /perfil/{usuarioId}`: Consulta de datos personales registrados.
- `PUT /perfil/{usuarioId}/direccion`: Alta/modificación de direcciones de recepción de compra.

---

## Microservicios Adicionales (Total: 10)

### 6. Servicio de Pagos (Pagos)
**Descripción:** Simula el procesamiento financiero internamente. No interactúa con pasarelas de pago reales externas, pero recrea el flujo lógico permitiendo transacciones de prueba (cobros) y devoluciones simuladas.
**Responsabilidades clave:** Simular autorización de cobros, procesar transacciones en base a un saldo o comportamiento lógico, emitir devoluciones/reembolsos simulados y registrar el historial financiero.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-amqp` (para emitir eventos al autorizar o devolver).
**Endpoints Principales:**
- `POST /pagos/procesar`: Recibe una solicitud de pago simulada, la aprueba y emite un evento de pago exitoso para notificar a la orden y el inventario.
- `POST /pagos/{transaccionId}/devolver`: Ejecuta el reembolso o devolución simulada de un pedido previo que ya había sido cobrado.

### 7. Servicio de Inventario (Inventario)
**Descripción:** Separar el catálogo del inventario asegura el control exacto de unidades sin penalizar búsquedas de catálogo.
**Responsabilidades clave:** Descontar/reservar stock durante compras, actualizar inventario, alertar sobre falta de stock.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-amqp` (RabbitMQ para escuchar eventos de pedidos), `spring-cloud-starter-netflix-eureka-client`.
**Endpoints Principales:**
- `GET /inventario/{productoId}`: Exponer cantidad cruda en almacenes.
- `POST /inventario/{productoId}/ingreso`: Agregar unidades a bodega.

### 8. Servicio de Envíos (Logística / Tracking)
**Descripción:** Encargado única y exclusivamente de la gestión logística.
**Responsabilidades clave:** Calcular tarifas de envío según peso/distancia y crear estados de tracking.
**Dependencias Maven Sugeridas:** `spring-boot-starter-web`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-amqp`.
**Endpoints Principales:**
- `POST /envios/cotizar`: Devuelve costo de envío en función al código postal.
- `GET /envios/seguimiento/{trackingId}`: Devuelve estado del paquete físico ("En reparto", "Entregado").

### 9. Eureka Server (Service Discovery)
**Descripción:** Actúa como un directorio de servicios. En lugar de comunicarse usando direcciones o IPs quemadas en el código, los servicios se registran aquí para encontrarse.
**Responsabilidades clave:** Registro de microservicios, descubrimiento de instancias y monitoreo del estado.
**Dependencias Maven Sugeridas:** `spring-cloud-starter-netflix-eureka-server`.
**Endpoints Principales:**
- *No provee endpoints REST para negocio. Posee un Web Dashboard en su puerto de levantamiento (ej: HTTP 8761).*

### 10. API Gateway
**Descripción:** Sirve como única puerta de enlace para todas las aplicaciones cliente. Redirige el tráfico y actúa como primera línea de seguridad, validando que el usuario envíe un JWT válido antes de permitirle pasar a los microservicios internos.
**Responsabilidades clave:** Validación global de Tokens (JWT Filters), enrutamiento, balanceo de carga, Cross-Origin (CORS).
**Dependencias Maven Sugeridas:** `spring-cloud-starter-gateway`, `spring-cloud-starter-netflix-eureka-client`, `jjwt-api` (para validar tokens pre-enrutamiento).
- *Intercepta y valida todas las llamadas (ej. comprobando la cabecera `Authorization: Bearer <token>`). Si el token es válido, hace forward a `/api/pedidos/**`, `/api/carrito/**`, etc. Para endpoints públicos como auth/login o el catálogo de productos, se configuran perfiles de acceso público dentro del Gateway.*

### 11. Servicio de Analíticas (Reportes)
**Descripción:** Se encarga de agregar y analizar datos, en especial ventas históricas extraídas de los pedidos, brindando métricas valiosas para la toma de decisiones.
**Responsabilidades clave:** Agrupación de ventas por fecha, cálculo de neto ganado y estadísticas gerenciales.
**Dependencias de Spring Initializr Necesarias:** 
Para generar el proyecto en Spring Initializr (zip), selecciona las siguientes dependencias:
- **Spring Web** (`spring-boot-starter-web`)
- **Eureka Discovery Client** (`spring-cloud-starter-netflix-eureka-client`)
- **OpenFeign** (`spring-cloud-starter-openfeign`)
- **Lombok** (`lombok`)

**Endpoints Principales:**
- `GET /api/reportes/ventas`: Devuelve un objeto con ingresos netos, cantidad de pedidos y distribución de ingresos por día.

---

## Interacción y Flujo de "Cruce de Servicios" (Gestión del Stock y Pedidos)

Para no crear bloqueos y asegurar velocidad en caso de caídas de ciertos proveedores, en el ecosistema Spring Boot se usa comunicación orientada a eventos, típicamente utilizando dependencias como **RabbitMQ o Apache Kafka**. 

A continuación se muestra en **qué momentos se cruzan los microservicios** durante un Checkout:

### Fase 1: Creación del Pedido y Reserva de Stock
1. El cliente desde el frontend ejecuta el checkout y el `API Gateway` hace enrutamiento hacia el microservicio **Pedidos**.
2. **Pedidos** llama de manera síncrona vía OpenFeign al servicio **Carrito** para obtener la lista de lo que será cobrado, y luego le ordena "Vaciar Carrito".
3. **Pedidos** crea el registro con STATUS = `PENDING_PAYMENT`.
4. El cruce más importante: **Pedidos** emite un evento a la cola llamado evento `OrderCreatedEvent`.
5. El servicio de **Inventario** está suscrito a ese evento, y automáticamente genera un "Bloque de Reserva", separando y restando esas X unidades temporalmente del inventario disponible (para evitar que alguien más lo compre).
   - *Consideración:* Si el inventario escucha que no hay unidades, responde con otro evento `OutOfStockEvent`, el cual capta el servicio de **Pedidos** y cancela automáticamente la compra.

### Fase 2: Confirmación de Pago y Descuento Definitivo
1. El cliente procesa su tarjeta y la pasarela interactúa con el servicio de **Pagos**.
2. **Pagos** registra el dinero a nivel local, y emite un evento `PaymentSuccessfulEvent`.
3. Ambos microservicios cruzan al escuchar este evento de forma paralela:
   - **Pedidos** lo escucha y actualiza su registro en base de datos al STATUS = `PAID`.
   - **Inventario** lo escucha y allí transforma "Bloque de Reserva" en una **deducción total y consolidada** en la base de datos de manera definitiva. 

### Fase 3: Logística
1. Cuando **Pedidos** ha actualizado su base a STATUS `PAID`, emite un evento intermedio `OrderReadyForShippingEvent`.
2. El servicio de **Envíos** captura este evento cruzado, y comienza el proceso real físico llamando al API de DHL/FedEx y guardando un código de guía (tracking). Generando que por consiguiente, el seguimiento inicie sin saturar el momento en el que el cliente dio click para pagar.

---

## Consideraciones Finales
- Configuración Centralizada (con *Spring Cloud Config Server*) sería el siguiente elemento de arquitectura más recomendable si los microservicios escalan fuertemente para centralizar todos los `application.yml`.
