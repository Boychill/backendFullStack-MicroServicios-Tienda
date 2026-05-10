package tienda.api.inventario.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CATALOGO")
public interface CatalogoClient {
    
    @PutMapping("/api/productos/{id}/stock")
    void actualizarStockEnCatalogo(
            @PathVariable("id") Long id, 
            @RequestParam("stock") Integer stock, 
            @RequestHeader("X-User-Role") String role
    );
}
