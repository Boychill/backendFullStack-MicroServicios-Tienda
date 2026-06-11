package tienda.api.pagos.dto;

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
public class ProcesarPagoResponseDto extends RepresentationModel<ProcesarPagoResponseDto> {
    private String status;
    private String transactionId;
    private String mensaje;
}
