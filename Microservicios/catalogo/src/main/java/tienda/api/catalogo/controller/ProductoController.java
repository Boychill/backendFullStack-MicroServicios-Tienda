package tienda.api.catalogo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.catalogo.model.Producto;
import tienda.api.catalogo.service.ProductoService;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(productoService.obtenerTodos(categoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        return productoService.obenterPorId(id)
                .map(producto -> ResponseEntity.ok((Object)producto))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No encontrado")));
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody Producto producto) {
        Producto nuevo = productoService.guardar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Producto creado", "productoId", nuevo.getId()));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStockSync(@PathVariable Long id, @RequestParam Integer stock) {
        productoService.actualizarStock(id, stock);
        return ResponseEntity.ok(Map.of("mensaje", "Stock de exhibición sincronizado síncronamente en el Catálogo a " + stock));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        productoService.cambiarEstadoProducto(id, activo);
        return ResponseEntity.ok(Map.of("mensaje", "Estado del producto actualizado a: " + (activo ? "Activo" : "Inactivo")));
    }
}
