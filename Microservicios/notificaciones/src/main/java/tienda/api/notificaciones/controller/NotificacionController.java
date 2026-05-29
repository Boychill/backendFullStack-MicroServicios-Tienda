package tienda.api.notificaciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tienda.api.notificaciones.service.NotificacionService;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    private Long getUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private String getRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
    }

    @GetMapping
    public ResponseEntity<?> obtenerMisNotificaciones() {
        return ResponseEntity.ok(notificacionService.obtenerMisNotificaciones(getUserId(), getRole()));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id, getUserId());
        return ResponseEntity.ok(Map.of("mensaje", "Notificación marcada como leída"));
    }
}
