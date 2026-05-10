package tienda.api.catalogo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tienda.api.catalogo.model.Producto;
import tienda.api.catalogo.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CatalogoSeeder implements CommandLineRunner {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productoRepository.count() == 0) {
            List<Producto> iniciales = List.of(
                    Producto.builder().nombre("Laptop Workstation").descripcion("16GB RAM, 512GB M.2").precio(new BigDecimal("1200.00")).stock(50).categoria("COMPUTACION").imagenUrl("http://img.com/laptop.png").build(),
                    Producto.builder().nombre("Monitor 27 pulgadas").descripcion("144Hz Full HD IPS").precio(new BigDecimal("250.00")).stock(30).categoria("ACCESORIOS").imagenUrl("http://img.com/monitor.png").build(),
                    Producto.builder().nombre("Silla Ergonómica").descripcion("Silla de oficina cómoda").precio(new BigDecimal("120.00")).stock(15).categoria("HOGAR").imagenUrl("http://img.com/silla.png").build()
            );
            productoRepository.saveAll(iniciales);
            System.out.println("====== [SEEDER] Catálogo base inicializado con éxito. ======");
        }
    }
}
