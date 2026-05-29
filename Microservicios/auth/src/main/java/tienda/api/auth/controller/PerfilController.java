package tienda.api.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.auth.model.Direccion;
import tienda.api.auth.service.PerfilService;
import java.util.Map;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    @Autowired private PerfilService perfilService;

    private Long getUserIdFromToken() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping("/direcciones/{id}")
    public ResponseEntity<?> obtenerDireccion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(perfilService.obtenerDireccion(getUserIdFromToken(), id));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/direcciones")
    public ResponseEntity<?> misDirecciones() {
        return ResponseEntity.ok(perfilService.listarMisDirecciones(getUserIdFromToken()));
    }

    @PostMapping("/direccion")
    public ResponseEntity<?> guardarDireccion(@Valid @RequestBody Direccion direccion) {
        try {
            return ResponseEntity.ok(perfilService.agregarDireccion(getUserIdFromToken(), direccion));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id) {
        try {
            perfilService.eliminarDireccion(getUserIdFromToken(), id);
            return ResponseEntity.ok(Map.of("mensaje", "Direccion borrada exitosamente"));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/direcciones/{id}")
    public ResponseEntity<?> modificarDireccion(@PathVariable Long id, @Valid @RequestBody Direccion direccionActualizada) {
        try {
            return ResponseEntity.ok(perfilService.actualizarDireccion(getUserIdFromToken(), id, direccionActualizada));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
