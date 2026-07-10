package tienda.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UsuarioDto usuario;
    @com.fasterxml.jackson.annotation.JsonProperty("_links")
    private Map<String, Object> _links = new HashMap<>();

    public AuthResponse(String token, String type, UsuarioDto usuario) {
        this.token = token;
        this.type = type;
        this.usuario = usuario;
    }

    public void addLink(String rel, Object link) {
        this._links.put(rel, link);
    }
}
