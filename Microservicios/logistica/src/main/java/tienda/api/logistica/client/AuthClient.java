package tienda.api.logistica.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "AUTH")
public interface AuthClient {

    @GetMapping("/api/auth/usuarios/choferes")
    List<Long> getChoferes();
}
