package tienda.api.inventario.dto;

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
public class AuditoriaStockResponseDto extends RepresentationModel<AuditoriaStockResponseDto> {
    private Long id;
    private Long productoId;
    private Long bodegaId;
    private Integer cantidadAfectada;
    private String tipoMovimiento;
    private String motivoReferencia;
    private LocalDateTime fechaMovimiento;
    private String responsableId;
}
