package tienda.api.inventario.dto;

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
public class BodegaResponseDto extends RepresentationModel<BodegaResponseDto> {
    private Long id;
    private String nombre;
    private String ubicacion;
    private Boolean activo;
}
