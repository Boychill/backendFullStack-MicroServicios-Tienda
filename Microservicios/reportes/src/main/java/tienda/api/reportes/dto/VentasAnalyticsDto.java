package tienda.api.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentasAnalyticsDto {
    private int totalPedidos;
    private BigDecimal ingresoNeto;
    private Map<String, BigDecimal> ventasPorFecha;
    private Long productoMasVendidoId;
    private Long totalUsuariosRegistrados;
}
