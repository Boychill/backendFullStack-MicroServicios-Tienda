package tienda.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.auth.model.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNombre(String nombre);
}
