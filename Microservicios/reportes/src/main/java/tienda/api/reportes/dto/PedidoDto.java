package tienda.api.reportes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PedidoDto {
    private Long id;
    
    @NotBlank(message = "El email del usuario es requerido")
    private Long usuarioId;
    
    @NotNull(message = "El total del pedido no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a cero")
    private BigDecimal total;
    
    @NotBlank(message = "El estado del pedido no puede estar vacío")
    private String estado;
    
    private LocalDateTime fechaCreacion;
    
    private java.util.List<ItemPedidoDto> items;
}
