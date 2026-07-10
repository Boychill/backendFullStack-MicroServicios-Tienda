package tienda.api.notificaciones.dto;

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

public class NotificacionResponseDto  {
    private Long id;
    private Long receptorId;
    private String rolReceptor;
    private String tipo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    @lombok.Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links") private java.util.Map<String, Object> _links = new java.util.HashMap<>();

    public void add(org.springframework.hateoas.Link link) {
        if (this._links == null) this._links = new java.util.HashMap<>();
        this._links.put(link.getRel().value(), link);
    }
}

