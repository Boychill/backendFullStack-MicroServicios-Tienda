# Referencia de la API (Microservicios Tienda)

Esta es la documentación de los endpoints disponibles en el sistema de microservicios, basada en las colecciones de Bruno.

Todas las rutas están prefijadas por el API Gateway (ejemplo: `http://localhost:8080`). Las rutas protegidas requieren de un token JWT (`Bearer Token`) en los headers de la petición.

---

## 1. Autenticación (Auth)

### 1.1 Iniciar Sesión (Login)
- **Método:** `POST`
- **Ruta:** `/api/auth/login`
- **Autenticación:** Ninguna
- **Cuerpo (JSON):**
  ```json
  {
    "email": "admin@tienda.com",
    "password": "123456"
  }
  ```

### 1.2 Registrar Usuario (Register)
- **Método:** `POST`
- **Ruta:** `/api/auth/register`
- **Autenticación:** Ninguna
- **Cuerpo (JSON):**
  ```json
  {
    "email": "nuevo_usuario@tienda.com",
    "password": "password123",
    "role": "ROLE_USER",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "direccionFiscal": "Av Siempreviva 742"
  }
  ```

---

## 2. Catálogo

### 2.1 Listar Productos
- **Método:** `GET`
- **Ruta:** `/api/productos`
- **Autenticación:** Ninguna

### 2.2 Crear Producto
- **Método:** `POST`
- **Ruta:** `/api/productos`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "nombre": "Laptop Pro",
    "precio": 1500.00,
    "categoria": "COMPUTACION"
  }
  ```

### 2.3 Cambiar Estado de Producto
- **Método:** `PUT`
- **Ruta:** `/api/productos/{id}/estado?activo=false`
- **Autenticación:** Requerida (Bearer Token)

---

## 3. Inventario

### 3.1 Crear Bodega
- **Método:** `POST`
- **Ruta:** `/api/inventario/bodegas`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "nombre": "Bodega Central",
    "ubicacionGeografica": "Centro Industrial"
  }
  ```

### 3.2 Ingreso de Stock
- **Método:** `POST`
- **Ruta:** `/api/inventario/ingreso`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "bodegaId": 1,
    "productoId": 1,
    "cantidadFisica": 50
  }
  ```

### 3.3 Auditoría de Stock
- **Método:** `GET`
- **Ruta:** `/api/inventario/auditoria/{productoId}`
- **Autenticación:** Requerida (Bearer Token)

---

## 4. Carrito

### 4.1 Ver Carrito
- **Método:** `GET`
- **Ruta:** `/api/carrito`
- **Autenticación:** Requerida (Bearer Token)

### 4.2 Agregar Ítem al Carrito
- **Método:** `POST`
- **Ruta:** `/api/carrito/items`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "productoId": 1,
    "cantidad": 2
  }
  ```

### 4.3 Reducir Ítem
- **Método:** `PUT`
- **Ruta:** `/api/carrito/items/{id}/reducir?cantidad=1`
- **Autenticación:** Requerida (Bearer Token)

### 4.4 Eliminar Ítem
- **Método:** `DELETE`
- **Ruta:** `/api/carrito/items/{id}`
- **Autenticación:** Requerida (Bearer Token)

### 4.5 Vaciar Carrito
- **Método:** `DELETE`
- **Ruta:** `/api/carrito/vaciar`
- **Autenticación:** Requerida (Bearer Token)

---

## 5. Pedidos

### 5.1 Checkout Total
- **Método:** `POST`
- **Ruta:** `/api/pedidos/checkout`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "numeroTarjeta": "4555888899990000"
  }
  ```

### 5.2 Checkout Parcial
- **Método:** `POST`
- **Ruta:** `/api/pedidos/checkout`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  {
    "numeroTarjeta": "4555888899990000",
    "productosSeleccionados": [
      {
        "productoId": 1,
        "cantidad": 1
      }
    ]
  }
  ```

### 5.3 Devolución
- **Método:** `POST`
- **Ruta:** `/api/pedidos/{id}/devolucion`
- **Autenticación:** Requerida (Bearer Token)
- **Cuerpo (JSON):**
  ```json
  [
    {
      "productoId": 1,
      "cantidad": 1
    }
  ]
  ```
