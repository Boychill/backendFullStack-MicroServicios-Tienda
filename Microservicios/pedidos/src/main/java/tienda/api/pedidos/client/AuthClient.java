package tienda.api.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;

@FeignClient(name = "AUTH")
public interface AuthClient {

    @GetMapping("/api/perfiles/direcciones/{id}")
    Map<String, Object> obtenerDireccion(@PathVariable("id") Long id, 
                                         @RequestHeader("X-User-Role") String role,
                                         @RequestHeader("X-User-Id") Long userId);
}
