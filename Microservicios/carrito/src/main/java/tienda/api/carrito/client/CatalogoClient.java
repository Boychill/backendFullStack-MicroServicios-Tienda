package tienda.api.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "CATALOGO")
public interface CatalogoClient {

    @GetMapping("/api/productos/{id}")
    Map<String, Object> obtenerProducto(@PathVariable("id") Long id);
}
