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
    
    private String getEmailFromToken() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<?> verCarrito() {
        String email = getEmailFromToken();
        return ResponseEntity.ok(carritoService.obtenerCarrito(email));
    }

    @PostMapping("/items")
    public ResponseEntity<?> agregarItem(@Valid @RequestBody AgregarItemRequest request) {
        String email = getEmailFromToken();
        var carrito = carritoService.agregarItem(email, request.getProductoId(), request.getCantidad(), request.getPrecio());
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<?> vaciarCarrito() {
        String email = getEmailFromToken();
        carritoService.vaciarCarrito(email);
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado"));
    }

    @PutMapping("/items/{productoId}/reducir")
    public ResponseEntity<?> reducirCantidad(@PathVariable Long productoId, @RequestParam Integer cantidad) {
        String email = getEmailFromToken();
        var carrito = carritoService.reducirCantidadItem(email, productoId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<?> eliminarItem(@PathVariable Long productoId) {
        String email = getEmailFromToken();
        var carrito = carritoService.eliminarItem(email, productoId);
        return ResponseEntity.ok(carrito);
    }
}
