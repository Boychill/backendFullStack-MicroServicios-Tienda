package tienda.api.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CarritoResponseDto extends RepresentationModel<CarritoResponseDto> {
    private Long id;
    private Long usuarioId;
    private BigDecimal total;
    private List<CartItemResponseDto> items;
}
