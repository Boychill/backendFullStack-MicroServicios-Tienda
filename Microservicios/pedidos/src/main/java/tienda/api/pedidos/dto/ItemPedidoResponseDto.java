package tienda.api.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoResponseDto {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
