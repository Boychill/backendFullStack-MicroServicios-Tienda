package tienda.api.pagos.dto;
import java.math.BigDecimal;

public class PagoRequest {
    private String numeroTarjeta;
    private BigDecimal montoTotal;

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
}
