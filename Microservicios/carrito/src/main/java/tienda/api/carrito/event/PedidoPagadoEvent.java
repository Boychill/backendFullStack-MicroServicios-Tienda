package tienda.api.carrito.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPagadoEvent {
    private Long pedidoId;
    private Long usuarioId;
    private String direccionCompleta;
    private List<ItemComprado> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemComprado {
        private Long productoId;
        private Integer cantidad;
    }
}
