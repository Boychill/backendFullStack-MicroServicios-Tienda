package tienda.api.pedidos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.pedidos.service.CheckoutService;
import tienda.api.pedidos.repository.PedidoRepository;
import tienda.api.pedidos.model.Pedido;
import tienda.api.pedidos.dto.CheckoutRequest;
import tienda.api.pedidos.dto.ItemCompra;
import tienda.api.pedidos.client.InventarioClient;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private InventarioClient inventarioClient;

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest payload) {
        try {
            var pedido = checkoutService.realizarCheckout(payload.getNumeroTarjeta(),
                    payload.getProductosSeleccionados());
            return ResponseEntity.ok(Map.of("mensaje", "Compra Exitosa", "orden", pedido));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/devolucion")
    public ResponseEntity<?> devolucion(@PathVariable Long id, @RequestBody List<ItemCompra> itemsADevolver) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            if (!"PAGADO".equals(pedido.getEstado())) {
                throw new RuntimeException("Solo se pueden devolver pedidos en estado PAGADO");
            }

            // Revertir inventario
            for (ItemCompra item : itemsADevolver) {
                inventarioClient.revertirDescuento(Map.of("productoId", item.getProductoId(), "cantidad",
                        item.getCantidad(), "ordenId", pedido.getId().toString()), "ROLE_ADMIN");
            }

            // Cambiar estado
            pedido.setEstado("REEMBOLSADO");
            pedidoRepository.save(pedido);

            return ResponseEntity.ok(Map.of("mensaje", "Devolución procesada y stock retornado a bodegas con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
