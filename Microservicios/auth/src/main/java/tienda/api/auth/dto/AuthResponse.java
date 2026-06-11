package tienda.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuthResponse extends RepresentationModel<AuthResponse> {
    private String token;
    private String type = "Bearer";
    private UsuarioDto usuario;
}
