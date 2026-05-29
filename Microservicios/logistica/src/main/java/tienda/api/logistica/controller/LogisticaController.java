package tienda.api.logistica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tienda.api.logistica.service.LogisticaService;

import java.util.Map;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {

    @Autowired
    private LogisticaService logisticaService;

    private Long getUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private boolean isManager() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LOGISTICA"));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes() {
        return ResponseEntity.ok(logisticaService.listarPendientes());
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> asignarChofer(@PathVariable Long id, @RequestParam(required = false) Long choferId) {
        Long idToAssign = choferId;
        if (idToAssign == null) {
            idToAssign = getUserId();
        } else if (!isManager()) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo los managers pueden asignar a otros choferes"));
        }
        return ResponseEntity.ok(logisticaService.asignarChofer(id, idToAssign));
    }

    @GetMapping("/mis-rutas")
    public ResponseEntity<?> listarMisRutas() {
        return ResponseEntity.ok(logisticaService.listarMisViajes(getUserId()));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        Long idForCheck = isManager() ? -1L : getUserId();
        return ResponseEntity.ok(logisticaService.actualizarEstado(id, estado, idForCheck));
    }

    // --- PANEL ADMIN / LOGISTICA ---

    @GetMapping("/rutas")
    public ResponseEntity<?> listarTodasLasRutas() {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        return ResponseEntity.ok(logisticaService.listarTodas());
    }

    @PutMapping("/{id}/reasignar")
    public ResponseEntity<?> reasignarChofer(@PathVariable Long id, @RequestParam Long nuevoChoferId) {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        return ResponseEntity.ok(logisticaService.reasignarChofer(id, nuevoChoferId));
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarRuta(@PathVariable Long id) {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        logisticaService.cancelarRuta(id);
        return ResponseEntity.ok(Map.of("mensaje", "Ruta cancelada exitosamente"));
    }

    @PostMapping("/auto-asignar")
    public ResponseEntity<?> autoAsignar() {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        try {
            logisticaService.autoAsignarRutas();
            return ResponseEntity.ok(Map.of("mensaje", "Rutas auto-asignadas exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
