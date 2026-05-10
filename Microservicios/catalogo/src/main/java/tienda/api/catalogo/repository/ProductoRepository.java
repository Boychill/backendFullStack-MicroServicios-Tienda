package tienda.api.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.catalogo.model.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaAndActivoTrue(String categoria);
    List<Producto> findByActivoTrue();
}
