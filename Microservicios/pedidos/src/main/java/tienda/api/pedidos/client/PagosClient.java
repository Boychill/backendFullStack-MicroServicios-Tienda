package tienda.api.pedidos.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "PAGOS")
public interface PagosClient {
    @PostMapping("/api/pagos/procesar")
    java.util.Map<String, String> procesarPago(@RequestBody java.util.Map<String, Object> request, @RequestHeader("X-User-Role") String role);
}
