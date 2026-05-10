package tienda.api.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.inventario.model.AuditoriaStock;
import java.util.List;

public interface AuditoriaStockRepository extends JpaRepository<AuditoriaStock, Long> {
    List<AuditoriaStock> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);
}
