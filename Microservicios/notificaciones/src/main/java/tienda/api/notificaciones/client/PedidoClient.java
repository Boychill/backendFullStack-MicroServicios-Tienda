package tienda.api.notificaciones.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import tienda.api.notificaciones.dto.PedidoDto;
import java.util.List;

@FeignClient(name = "PEDIDOS")
public interface PedidoClient {
    
    @GetMapping("/api/pedidos")
    List<PedidoDto> obtenerTodosLosPedidos();
}
