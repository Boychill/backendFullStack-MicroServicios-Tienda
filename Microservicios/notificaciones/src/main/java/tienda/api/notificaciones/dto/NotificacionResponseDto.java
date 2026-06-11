package tienda.api.notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NotificacionResponseDto extends RepresentationModel<NotificacionResponseDto> {
    private Long id;
    private Long receptorId;
    private String rolReceptor;
    private String tipo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
}
