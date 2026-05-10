package tienda.api.reportes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import tienda.api.reportes.dto.PedidoDto;
import java.util.List;

@FeignClient(name = "PEDIDOS")
public interface PedidoClient {
    
    @GetMapping("/api/pedidos")
    List<PedidoDto> obtenerTodosLosPedidos();
}
