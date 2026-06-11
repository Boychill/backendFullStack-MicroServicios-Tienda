package tienda.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.auth.dto.DireccionResponseDto;
import tienda.api.auth.model.Direccion;
import tienda.api.auth.service.PerfilService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/perfiles")
@Tag(name = "Perfiles y Direcciones", description = "Endpoints para la gestión de direcciones del usuario")
public class PerfilController {

    @Autowired private PerfilService perfilService;

    private Long getUserIdFromToken() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private DireccionResponseDto convertToDto(Direccion direccion) {
        DireccionResponseDto dto = DireccionResponseDto.builder()
            .id(direccion.getId())
            .usuarioId(direccion.getUsuarioId())
            .alias(direccion.getAlias())
            .direccionEscrita(direccion.getDireccionEscrita())
            .latitud(direccion.getLatitud())
            .longitud(direccion.getLongitud())
            .esPrincipal(direccion.getEsPrincipal())
            .build();
            
        dto.add(linkTo(methodOn(PerfilController.class).obtenerDireccion(direccion.getId())).withSelfRel());
        dto.add(linkTo(methodOn(PerfilController.class).misDirecciones()).withRel("todas_mis_direcciones"));
        return dto;
    }

    @GetMapping("/direcciones/{id}")
    @Operation(summary = "Obtener dirección", description = "Obtiene una dirección específica por ID")
    @ApiResponse(responseCode = "200", description = "Dirección obtenida exitosamente")
    public ResponseEntity<?> obtenerDireccion(@PathVariable Long id) {
        try {
            Direccion dir = perfilService.obtenerDireccion(getUserIdFromToken(), id);
            return ResponseEntity.ok(convertToDto(dir));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/direcciones")
    @Operation(summary = "Listar mis direcciones", description = "Obtiene todas las direcciones asociadas al usuario autenticado")
    public ResponseEntity<List<DireccionResponseDto>> misDirecciones() {
        List<Direccion> direcciones = perfilService.listarMisDirecciones(getUserIdFromToken());
        List<DireccionResponseDto> dtos = direcciones.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/direccion")
    @Operation(summary = "Guardar dirección", description = "Crea una nueva dirección para el usuario")
    public ResponseEntity<?> guardarDireccion(@Valid @RequestBody Direccion direccion) {
        try {
            Direccion dir = perfilService.agregarDireccion(getUserIdFromToken(), direccion);
            return ResponseEntity.ok(convertToDto(dir));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/direcciones/{id}")
    @Operation(summary = "Eliminar dirección", description = "Elimina una dirección específica del usuario")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id) {
        try {
            perfilService.eliminarDireccion(getUserIdFromToken(), id);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Direccion borrada exitosamente",
                "_links", Map.of("mis_direcciones", linkTo(methodOn(PerfilController.class).misDirecciones()).withRel("mis_direcciones"))
            ));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/direcciones/{id}")
    @Operation(summary = "Modificar dirección", description = "Actualiza los datos de una dirección existente")
    public ResponseEntity<?> modificarDireccion(@PathVariable Long id, @Valid @RequestBody Direccion direccionActualizada) {
        try {
            Direccion dir = perfilService.actualizarDireccion(getUserIdFromToken(), id, direccionActualizada);
            return ResponseEntity.ok(convertToDto(dir));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
