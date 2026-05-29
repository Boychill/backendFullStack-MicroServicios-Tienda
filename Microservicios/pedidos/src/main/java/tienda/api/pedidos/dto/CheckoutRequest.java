package tienda.api.pedidos.dto;

import java.util.List;

public class CheckoutRequest {
    private Long direccionId;
    public Long getDireccionId() { return direccionId; }
    public void setDireccionId(Long d) { this.direccionId = d; }
    private String numeroTarjeta;
    private List<ItemCompra> productosSeleccionados;

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }
    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }
    public List<ItemCompra> getProductosSeleccionados() {
        return productosSeleccionados;
    }
    public void setProductosSeleccionados(List<ItemCompra> productosSeleccionados) {
        this.productosSeleccionados = productosSeleccionados;
    }
}
