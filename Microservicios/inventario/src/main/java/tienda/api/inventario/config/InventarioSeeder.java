package tienda.api.inventario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tienda.api.inventario.model.Bodega;
import tienda.api.inventario.dto.IngresoRequest;
import tienda.api.inventario.repository.BodegaRepository;
import tienda.api.inventario.service.InventarioService;

@Component
public class InventarioSeeder implements CommandLineRunner {

    @Autowired private BodegaRepository bodegaRepository;
    @Autowired private InventarioService inventarioService;

    @Override
    public void run(String... args) throws Exception {
        if (bodegaRepository.count() == 0) {
            Bodega central = new Bodega();
            central.setNombre("Bodega Central Santiago");
            central.setUbicacion("Santiago Centro");
            central.setActivo(true);
            central = bodegaRepository.save(central);
            
            Bodega norte = new Bodega();
            norte.setNombre("Bodega Norte Iquique");
            norte.setUbicacion("Iquique, ZOFRI");
            norte.setActivo(true);
            bodegaRepository.save(norte);

            // Seed laptops to Central (Id: 1)
            IngresoRequest req1 = new IngresoRequest();
            req1.setBodegaId(central.getId());
            req1.setProductoId(1L);
            req1.setCantidadFisica(20);
            inventarioService.registrarIngreso(req1);

            // Seed laptops to Norte (Id: 2) too to test Multi-logic
            IngresoRequest req2 = new IngresoRequest();
            req2.setBodegaId(norte.getId());
            req2.setProductoId(1L);
            req2.setCantidadFisica(5);
            inventarioService.registrarIngreso(req2);
            
            // Monitors to Central
            IngresoRequest req3 = new IngresoRequest();
            req3.setBodegaId(central.getId());
            req3.setProductoId(2L);
            req3.setCantidadFisica(15);
            inventarioService.registrarIngreso(req3);
            
            System.out.println("SEEDER MULTI-BODEGA: Inventarios Inicializados sobre 2 nodos logísticos.");
        }
    }
}
