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

    private String getEmailFromToken() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/direcciones")
    public ResponseEntity<?> misDirecciones() {
        return ResponseEntity.ok(perfilService.listarMisDirecciones(getEmailFromToken()));
    }

    @PostMapping("/direccion")
    public ResponseEntity<?> guardarDireccion(@Valid @RequestBody Direccion direccion) {
        try {
            return ResponseEntity.ok(perfilService.agregarDireccion(getEmailFromToken(), direccion));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id) {
        try {
            perfilService.eliminarDireccion(getEmailFromToken(), id);
            return ResponseEntity.ok(Map.of("mensaje", "Dirección borrada exitosamente"));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
