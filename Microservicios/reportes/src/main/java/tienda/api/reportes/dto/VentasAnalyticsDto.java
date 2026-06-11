package tienda.api.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VentasAnalyticsDto extends RepresentationModel<VentasAnalyticsDto> {
    private int totalPedidos;
    private BigDecimal ingresoNeto;
    private Map<String, BigDecimal> ventasPorFecha;
    private Long productoMasVendidoId;
    private Long totalUsuariosRegistrados;
}
