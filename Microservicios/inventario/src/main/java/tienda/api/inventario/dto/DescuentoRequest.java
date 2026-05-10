package tienda.api.inventario.dto;

import lombok.Data;

@Data
public class DescuentoRequest {
    private Long productoId;
    private Integer cantidadDescontar;
    private String ordenId;
}
