package tienda.api.inventario.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.inventario.model.Bodega;
public interface BodegaRepository extends JpaRepository<Bodega, Long> { }
