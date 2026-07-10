package tienda.api.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProcesarPagoResponseDto  {
    private String status;
    private String transactionId;
    private String mensaje;
    @lombok.Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links") private java.util.Map<String, Object> _links = new java.util.HashMap<>();

    public void add(org.springframework.hateoas.Link link) {
        if (this._links == null) this._links = new java.util.HashMap<>();
        this._links.put(link.getRel().value(), link);
    }
}

