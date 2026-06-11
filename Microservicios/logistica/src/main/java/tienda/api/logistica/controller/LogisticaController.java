package tienda.api.logistica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tienda.api.logistica.dto.GuiaDespachoResponseDto;
import tienda.api.logistica.model.GuiaDespacho;
import tienda.api.logistica.service.LogisticaService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/logistica")
@Tag(name = "Logística y Despacho", description = "Endpoints para la gestión de envíos y bodegueros")
public class LogisticaController {

    @Autowired
    private LogisticaService logisticaService;

    private Long getUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private boolean isManager() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LOGISTICA") || a.getAuthority().equals("ROLE_BODEGUERO"));
    }

    private GuiaDespachoResponseDto convertToDto(GuiaDespacho guia) {
        GuiaDespachoResponseDto dto = GuiaDespachoResponseDto.builder()
            .id(guia.getId())
            .pedidoId(guia.getPedidoId())
            .direccionCompleta(guia.getDireccionCompleta())
            .choferId(guia.getChoferId())
            .estado(guia.getEstado())
            .fechaCreacion(guia.getFechaCreacion())
            .fechaActualizacion(guia.getFechaActualizacion())
            .build();

        if (guia.getEstado().equals("PENDIENTE")) {
            dto.add(linkTo(methodOn(LogisticaController.class).marcarComoArmado(guia.getId())).withRel("armar"));
        } else if (guia.getEstado().equals("ARMADO")) {
            dto.add(linkTo(methodOn(LogisticaController.class).asignarChofer(guia.getId(), null)).withRel("asignar_chofer"));
        } else if (guia.getEstado().equals("EN_RUTA") || guia.getEstado().equals("ASIGNADO")) {
            dto.add(linkTo(methodOn(LogisticaController.class).actualizarEstado(guia.getId(), "ENTREGADO")).withRel("marcar_entregado"));
        }

        return dto;
    }

    private List<GuiaDespachoResponseDto> convertList(List<GuiaDespacho> guias) {
        return guias.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @GetMapping("/bodega/pendientes")
    @Operation(summary = "Listar para armar", description = "Lista todos los pedidos pendientes por ser armados en bodega")
    public ResponseEntity<List<GuiaDespachoResponseDto>> listarParaArmar() {
        return ResponseEntity.ok(convertList(logisticaService.listarPendientes()));
    }

    @PutMapping("/bodega/{id}/armar")
    @Operation(summary = "Marcar como armado", description = "Bodega marca un paquete como empaquetado/armado")
    public ResponseEntity<?> marcarComoArmado(@PathVariable Long id) {
        try {
            GuiaDespacho guia = logisticaService.marcarComoArmado(id);
            return ResponseEntity.ok(convertToDto(guia));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar despachos pendientes", description = "Lista todas las guías pendientes de despacho")
    public ResponseEntity<List<GuiaDespachoResponseDto>> listarPendientes() {
        return ResponseEntity.ok(convertList(logisticaService.listarPendientes()));
    }

    @PutMapping("/{id}/asignar")
    @Operation(summary = "Asignar chofer", description = "Asigna un viaje a un chofer específico o a sí mismo")
    public ResponseEntity<?> asignarChofer(@PathVariable Long id, @RequestParam(required = false) Long choferId) {
        Long idToAssign = choferId;
        if (idToAssign == null) {
            idToAssign = getUserId();
        } else if (!isManager()) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo los managers pueden asignar a otros choferes"));
        }
        GuiaDespacho guia = logisticaService.asignarChofer(id, idToAssign);
        return ResponseEntity.ok(convertToDto(guia));
    }

    @GetMapping("/mis-rutas")
    @Operation(summary = "Listar mis rutas", description = "Lista las rutas asignadas al chofer autenticado")
    public ResponseEntity<List<GuiaDespachoResponseDto>> listarMisRutas() {
        return ResponseEntity.ok(convertList(logisticaService.listarMisViajes(getUserId())));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de una guía de despacho (e.g. ENTREGADO)")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        Long idForCheck = isManager() ? -1L : getUserId();
        GuiaDespacho guia = logisticaService.actualizarEstado(id, estado, idForCheck);
        return ResponseEntity.ok(convertToDto(guia));
    }

    // --- PANEL ADMIN / LOGISTICA ---

    @GetMapping("/rutas")
    @Operation(summary = "Listar todas las rutas (Admin)", description = "Lista todas las guías de despacho. Solo para roles Manager.")
    public ResponseEntity<?> listarTodasLasRutas() {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        return ResponseEntity.ok(convertList(logisticaService.listarTodas()));
    }

    @PutMapping("/{id}/reasignar")
    @Operation(summary = "Reasignar chofer (Admin)", description = "Cambia el chofer de un despacho en progreso. Solo Managers.")
    public ResponseEntity<?> reasignarChofer(@PathVariable Long id, @RequestParam Long nuevoChoferId) {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        GuiaDespacho guia = logisticaService.reasignarChofer(id, nuevoChoferId);
        return ResponseEntity.ok(convertToDto(guia));
    }

    @DeleteMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar ruta (Admin)", description = "Cancela una guía de despacho permanentemente.")
    public ResponseEntity<?> cancelarRuta(@PathVariable Long id) {
        if (!isManager()) return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo managers."));
        logisticaService.cancelarRuta(id);
        return ResponseEntity.ok(Map.of("mensaje", "Ruta cancelada exitosamente"));
    }

    @PostMapping("/auto-asignar")
    @Operation(summary = "Auto-asignar rutas (Admin)", description = "Distribuye rutas automáticamente entre choferes disponibles.")
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
