package tienda.api.pedidos.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@FeignClient(name = "INVENTARIO")
public interface InventarioClient {
    @PostMapping("/api/inventario/descuento-lote")
    java.util.Map<String, String> descontarStockLote(@RequestBody java.util.Map<String, Object> request, @RequestHeader("X-User-Role") String role);

    @PostMapping("/api/inventario/revertir")
    java.util.Map<String, String> revertirDescuento(@RequestBody java.util.Map<String, Object> request, @RequestHeader("X-User-Role") String role);
}
