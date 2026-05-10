package tienda.api.inventario.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tienda.api.inventario.model.InventarioBodega;
import java.util.List;
import java.util.Optional;

public interface InventarioBodegaRepository extends JpaRepository<InventarioBodega, Long> {
    Optional<InventarioBodega> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);
    List<InventarioBodega> findByProductoId(Long productoId);
    
    @Query("SELECT SUM(i.cantidadDisponible) FROM InventarioBodega i WHERE i.productoId = :productoId")
    Integer sumStockByProductoId(@Param("productoId") Long productoId);
}
