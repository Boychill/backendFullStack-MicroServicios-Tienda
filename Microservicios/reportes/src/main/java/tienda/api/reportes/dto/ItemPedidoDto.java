package tienda.api.reportes.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemPedidoDto {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
