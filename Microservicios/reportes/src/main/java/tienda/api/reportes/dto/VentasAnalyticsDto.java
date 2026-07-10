package tienda.api.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class VentasAnalyticsDto  {
    private int totalPedidos;
    private BigDecimal ingresoNeto;
    private Map<String, BigDecimal> ventasPorFecha;
    private Long productoMasVendidoId;
    private Long totalUsuariosRegistrados;
    @lombok.Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links") private java.util.Map<String, Object> _links = new java.util.HashMap<>();

    public void add(org.springframework.hateoas.Link link) {
        if (this._links == null) this._links = new java.util.HashMap<>();
        this._links.put(link.getRel().value(), link);
    }
}

