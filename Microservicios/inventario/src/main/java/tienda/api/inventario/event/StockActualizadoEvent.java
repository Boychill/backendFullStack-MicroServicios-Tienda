package tienda.api.inventario.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockActualizadoEvent {
    private Long productoId;
    private Integer stock;
}
