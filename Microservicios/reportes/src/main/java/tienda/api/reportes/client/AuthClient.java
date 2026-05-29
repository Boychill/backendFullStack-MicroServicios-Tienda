package tienda.api.reportes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@FeignClient(name = "AUTH")
public interface AuthClient {
    
    @GetMapping("/api/auth/count")
    Map<String, Long> obtenerTotalUsuarios();
}
