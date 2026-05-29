# Referencia de la API (Microservicios Tienda)

Esta es la documentación de los endpoints disponibles en el ecosistema, basándonos en la Colección de Bruno del proyecto. Todas las rutas están prefijadas por el API Gateway (`http://localhost:8080`).

---

## 1. Auth & Perfiles
- `POST /api/auth/login` (Público): Autenticación y generación de JWT Bearer.
- `POST /api/auth/register` (Público): Registro de un nuevo usuario encriptando la clave.
- `GET /api/perfiles/direcciones`: Retorna el listado de direcciones postales de entrega del usuario autenticado.
- `POST /api/perfiles/direccion`: Agrega una nueva dirección a la libreta del usuario.
- `PUT /api/perfiles/direcciones/{id}`: Modifica una dirección existente.
- `DELETE /api/perfiles/direcciones/{id}`: Elimina una dirección.

## 2. Catálogo
- `GET /api/productos` (Público): Lista los productos y sus precios.
- `POST /api/productos`: Crea un producto nuevo (Ruta protegida, requiere Administrador).
- `PUT /api/productos/{id}/estado`: Activa o desactiva un producto de la vista pública.

## 3. Inventario
- `POST /api/inventario/bodegas`: Crea una nueva bodega física en el sistema.
- `POST /api/inventario/ingreso`: Añade unidades físicas (stock) a un producto dentro de una bodega específica, sincronizando al instante la visibilidad en el Catálogo.
- `GET /api/inventario/auditoria/{productoId}`: Revisa exactamente el historial de movimientos de un producto. Registra con precisión la `bodegaId` afectada y tipifica los movimientos (INGRESO, EGRESO o DEVOLUCION).

## 4. Carrito
- `GET /api/carrito`: Ve los items actuales en tu sesión.
- `POST /api/carrito/items`: Añade un producto a tu carrito. *(Nota: Valida en tiempo real que exista stock físico disponible en el microservicio Catálogo. Lanza excepción si excede el stock)*.
- `PUT /api/carrito/items/{id}/reducir`: Reduce la cantidad de un ítem.
- `DELETE /api/carrito/items/{id}`: Saca el ítem del carrito.
- `DELETE /api/carrito/vaciar`: Elimina todo (Esto también lo llama automáticamente `Pedidos` tras un cobro exitoso).

## 5. Pedidos (Orquestador SAGA)
- `POST /api/pedidos/checkout`: Ejecuta el flujo transaccional. Revisa stock en Inventario, cobra en Pagos y limpia el Carrito. Devuelve el número de orden generado.
- `GET /api/pedidos/mis-pedidos`: Retorna el historial de compras completo del usuario logueado. Es la fuente de la verdad para ver el estado dinámico (Pagado, En Ruta, Entregado, etc.).
- `POST /api/pedidos/{id}/devolucion`: Inicia el flujo inverso manual para devolver el dinero y el stock a las bodegas.

## 6. Logística
- `GET /api/logistica/admin/rutas`: Panel de administrador para ver todas las rutas generadas.
- `GET /api/logistica/chofer/mis-rutas`: Panel del chofer para ver qué pedidos le toca entregar hoy (Filtrado por token de Rol Chofer).
- `PUT /api/logistica/admin/rutas/{id}/reasignar`: Permite que un admin cambie al chofer encargado del envío.
- `PUT /api/logistica/admin/rutas/{id}/cancelar`: El admin cancela la ruta antes de iniciar el viaje.
- `PUT /api/logistica/chofer/rutas/{id}/estado`: El chofer reporta el estado final del paquete (`ENTREGADO`, `NO_RESPUESTA`). Esta llamada activa el RabbitMQ que actualiza todo el sistema automáticamente.

## 7. Reportes (Analytics)
- `GET /api/reportes/ventas`: Agrega los datos de todo el ecosistema (Pedidos y Auth) al vuelo, para indicarle a los directivos los ingresos netos de la empresa y **calcula mediante algoritmos cuál es el ID del Producto Más Vendido en el histórico**.

## 8. Notificaciones
- `GET /api/notificaciones`: Muestra las alertas recibidas por el usuario logueado en su campanita.
- `PUT /api/notificaciones/{id}/leer`: Marca una notificación como vista.

## 9. Pagos
- `POST /api/pagos/procesar`: Endpoint fantasma que simula el cobro a la tarjeta.
- `POST /api/pagos/reembolso/{id}`: El administrador fuerza una devolución de la tarjeta del cliente. Este endpoint le avisa por RabbitMQ a `Pedidos` que marque la orden como `REEMBOLSADO` de forma automatizada.
