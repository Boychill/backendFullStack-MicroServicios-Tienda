package tienda.api.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CARRITO")
public interface CarritoClient {
    @GetMapping("/api/carrito")
    java.util.Map<String, Object> obtenerCarrito(@RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long id);

    @DeleteMapping("/api/carrito/vaciar")
    void vaciarCarrito(@RequestHeader("X-User-Role") String role, @RequestHeader("X-User-Id") Long id);

    @org.springframework.web.bind.annotation.PutMapping("/api/carrito/items/{productoId}/reducir")
    void reducirCantidadItem(@org.springframework.web.bind.annotation.PathVariable("productoId") Long productoId, 
                             @org.springframework.web.bind.annotation.RequestParam("cantidad") Integer cantidad,
                             @RequestHeader("X-User-Role") String role, 
                             @RequestHeader("X-User-Id") Long id);
}
