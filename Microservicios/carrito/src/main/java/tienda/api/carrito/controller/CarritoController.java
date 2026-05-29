package tienda.api.carrito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.carrito.dto.AgregarItemRequest;
import tienda.api.carrito.service.CarritoService;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    
    private Long getUserIdFromToken() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping
    public ResponseEntity<?> verCarrito() {
        Long usuarioId = getUserIdFromToken();
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> agregarItem(@Valid @RequestBody AgregarItemRequest request) {
        Long usuarioId = getUserIdFromToken();
        var carrito = carritoService.agregarItem(usuarioId, request.getProductoId(), request.getCantidad());
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<?> vaciarCarrito() {
        Long usuarioId = getUserIdFromToken();
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado"));
    }

    @PutMapping("/items/{productoId}/reducir")
    public ResponseEntity<?> reducirCantidad(@PathVariable Long productoId, @RequestParam Integer cantidad) {
        Long usuarioId = getUserIdFromToken();
        var carrito = carritoService.reducirCantidadItem(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<?> eliminarItem(@PathVariable Long productoId) {
        Long usuarioId = getUserIdFromToken();
        var carrito = carritoService.eliminarItem(usuarioId, productoId);
        return ResponseEntity.ok(carrito);
    }
}
