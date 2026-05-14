package tienda.api.catalogo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "INVENTARIO")
public interface InventarioClient {

    @PutMapping("/api/inventario/productos/{id}/estado")
    void desactivarProductoEnInventario(@PathVariable("id") Long id, @RequestParam("activo") Boolean activo, @RequestHeader("X-User-Role") String rol);
}
