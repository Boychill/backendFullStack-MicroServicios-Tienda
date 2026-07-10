package tienda.api.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PedidoResponseDto  {
    private Long id;
    private Long usuarioId;
    private BigDecimal total;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaCreacion;
    private String direccionCompleta;
    private List<ItemPedidoResponseDto> items;
    @lombok.Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links") private java.util.Map<String, Object> _links = new java.util.HashMap<>();

    public void add(org.springframework.hateoas.Link link) {
        if (this._links == null) this._links = new java.util.HashMap<>();
        this._links.put(link.getRel().value(), link);
    }
}

