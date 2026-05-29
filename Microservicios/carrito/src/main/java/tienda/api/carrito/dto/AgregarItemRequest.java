package tienda.api.carrito.dto;

import java.math.BigDecimal;

public class AgregarItemRequest {
    private Long productoId;
    private Integer cantidad;
    

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    
}
