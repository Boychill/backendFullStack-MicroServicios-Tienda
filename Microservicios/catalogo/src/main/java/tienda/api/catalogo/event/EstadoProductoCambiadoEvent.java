package tienda.api.catalogo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoProductoCambiadoEvent {
    private Long productoId;
    private Boolean activo;
}
