package tienda.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.auth.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
