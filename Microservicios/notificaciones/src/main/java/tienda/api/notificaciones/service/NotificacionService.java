package tienda.api.notificaciones.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.notificaciones.model.Notificacion;
import tienda.api.notificaciones.repository.NotificacionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    public Notificacion crearNotificacion(Long receptorId, String rolReceptor, String tipo, String mensaje) {
        Notificacion notif = Notificacion.builder()
                .receptorId(receptorId)
                .rolReceptor(rolReceptor)
                .tipo(tipo)
                .mensaje(mensaje)
                .fechaCreacion(LocalDateTime.now())
                .leida(false)
                .build();
        return notificacionRepository.save(notif);
    }

    public List<Notificacion> obtenerMisNotificaciones(Long receptorId, String role) {
        if (role.contains("ROLE_ADMIN")) {
            return notificacionRepository.findByRolReceptorOrderByFechaCreacionDesc("ROLE_ADMIN");
        }
        return notificacionRepository.findByReceptorIdOrderByFechaCreacionDesc(receptorId);
    }

    public void marcarComoLeida(Long id, Long receptorId) {
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notif.getReceptorId().equals(receptorId) && !notif.getRolReceptor().equals("ROLE_ADMIN")) {
            throw new RuntimeException("No tienes permiso");
        }
        notif.setLeida(true);
        notificacionRepository.save(notif);
    }
}
