package tienda.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DireccionResponseDto extends RepresentationModel<DireccionResponseDto> {
    private Long id;
    private Long usuarioId;
    private String alias;
    private String direccionEscrita;
    private Double latitud;
    private Double longitud;
    private Boolean esPrincipal;
}
