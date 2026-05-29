package tienda.api.notificaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.notificaciones.model.Notificacion;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByReceptorIdOrderByFechaCreacionDesc(Long receptorId);
    List<Notificacion> findByRolReceptorOrderByFechaCreacionDesc(String rol);
}
