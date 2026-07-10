package tienda.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponseDto {
    private Long id;
    private Long usuarioId;
    private String alias;
    private String direccionEscrita;
    private Double latitud;
    private Double longitud;
    private Boolean esPrincipal;
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("_links")
    private Map<String, Object> _links = new HashMap<>();

    public void addLink(String rel, Object link) {
        if (this._links == null) this._links = new HashMap<>();
        this._links.put(rel, link);
    }
}
