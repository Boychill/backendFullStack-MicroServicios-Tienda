package tienda.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.auth.model.Direccion;
import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuarioEmail(String email);
}
