package tienda.api.inventario.dto;
public class IngresoRequest {
    private Long bodegaId;
    private Long productoId;
    private Integer cantidadFisica;

    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getCantidadFisica() { return cantidadFisica; }
    public void setCantidadFisica(Integer cantidadFisica) { this.cantidadFisica = cantidadFisica; }
}
