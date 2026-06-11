package tienda.api.notificaciones.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tienda.api.notificaciones.dto.NotificacionResponseDto;
import tienda.api.notificaciones.model.Notificacion;
import tienda.api.notificaciones.service.NotificacionService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para consultar y marcar notificaciones del usuario")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    private Long getUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private String getRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
    }

    private NotificacionResponseDto convertToDto(Notificacion noti) {
        NotificacionResponseDto dto = NotificacionResponseDto.builder()
            .id(noti.getId())
            .receptorId(noti.getReceptorId())
            .rolReceptor(noti.getRolReceptor())
            .tipo(noti.getTipo())
            .mensaje(noti.getMensaje())
            .leida(noti.isLeida())
            .fechaCreacion(noti.getFechaCreacion())
            .build();

        if (!noti.isLeida()) {
            dto.add(linkTo(methodOn(NotificacionController.class).marcarComoLeida(noti.getId())).withRel("marcar_leida"));
        }
        return dto;
    }

    @GetMapping
    @Operation(summary = "Obtener mis notificaciones", description = "Obtiene las notificaciones (leídas y no leídas) del usuario o rol autenticado")
    public ResponseEntity<List<NotificacionResponseDto>> obtenerMisNotificaciones() {
        List<Notificacion> notificaciones = notificacionService.obtenerMisNotificaciones(getUserId(), getRole());
        List<NotificacionResponseDto> dtos = notificaciones.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar como leída", description = "Marca una notificación específica como leída")
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída exitosamente")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id, getUserId());
        return ResponseEntity.ok(Map.of(
            "mensaje", "Notificación marcada como leída",
            "_links", Map.of("mis_notificaciones", linkTo(methodOn(NotificacionController.class).obtenerMisNotificaciones()).withRel("mis_notificaciones"))
        ));
    }
}
