package tienda.api.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PedidoResponseDto extends RepresentationModel<PedidoResponseDto> {
    private Long id;
    private Long usuarioId;
    private BigDecimal total;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaCreacion;
    private String direccionCompleta;
    private List<ItemPedidoResponseDto> items;
}
