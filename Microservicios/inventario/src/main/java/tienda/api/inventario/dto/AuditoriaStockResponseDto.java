package tienda.api.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuditoriaStockResponseDto  {
    private Long id;
    private Long productoId;
    private Long bodegaId;
    private Integer cantidadAfectada;
    private String tipoMovimiento;
    private String motivoReferencia;
    private LocalDateTime fechaMovimiento;
    private String responsableId;
    @lombok.Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links") private java.util.Map<String, Object> _links = new java.util.HashMap<>();

    public void add(org.springframework.hateoas.Link link) {
        if (this._links == null) this._links = new java.util.HashMap<>();
        this._links.put(link.getRel().value(), link);
    }
}

